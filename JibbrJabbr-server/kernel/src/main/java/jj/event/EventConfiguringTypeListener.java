/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.event;

import io.netty.util.internal.PlatformDependent;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * <p>
 * The heart of the event system.  Basically,
 * <ul>
 * <li>All bindings are introspected for a Subscriber annotation
 * <li>if one is found, all non-static, non-private methods of
 *     the bound class that take a single parameter are checked
 *     for Listener annotations.  
 * <li>if one or more are found, then when instances of the class
 *     are created, they are bound up as listeners so the EventManager
 *     can publish events to them
 * </ul> 
 * 
 * That's a little light on the details but substantially correct.
 * </p>
 * @author jason
 *
 */
class EventConfiguringTypeListener implements TypeListener {
	
	private final Logger logger = LoggerFactory.getLogger(EventConfiguringTypeListener.class);

	/**
	 * mapped from subscriber class name -> the methodinfos for the listener methods of the class. 
	 * since this list stored as the value is only ever manipulated when a new type is encountered for
	 * the first time, and is read-only after that, it doesn't need to be synchronized
	 */
	private final ConcurrentMap<String, List<MethodInfo>> subscribers = PlatformDependent.newConcurrentHashMap();
	
	/** 
	 * mapped from the class name of an invoker to the invoker class.  wait, what? 
	 * well it doesn't just rely on class.forname because it needs to keep track of
	 * which classes have already been generated so the CtClass objects can be
	 * cleaned up
	 */
	private final ConcurrentMap<String, Class<? extends Invoker>> invokerClasses = PlatformDependent.newConcurrentHashMap();
	
	/** 
	 * mapped from the event type -> the set of listener invokers for that event 
	 * the set stored as the value can be manipulated from multiple threads, so it needs
	 * to also be concurrent, but the value itself will only ever be set once on first encounter
	 * for a given event type and never removed after so that should do it
	 */
	private final ConcurrentMap<Class<?>, Set<Invoker>> invokers = PlatformDependent.newConcurrentHashMap();
	
	/** mapped from the weak reference to the instance invoked -> invoker */
	private final ConcurrentMap<WeakReference<Object>, Invoker> cleanupMap = PlatformDependent.newConcurrentHashMap();
	
	private final ReferenceQueue<Object> invokerInstanceQueue = new ReferenceQueue<>();
	
	private final ClassPool classPool;
	private final CtClass invokerClass;
	private final CtMethod invokeMethod;
	
	EventConfiguringTypeListener() {
		try {
			classPool = new ClassPool();
			classPool.appendClassPath(new LoaderClassPath(EventConfiguringTypeListener.class.getClassLoader()));
			
			invokerClass = classPool.get("jj.event.Invoker");
			invokeMethod = invokerClass.getDeclaredMethod("invoke");
			Thread queueCleaner = new Thread(new ListenerCleaner(), "Event System cleanup");
			queueCleaner.setDaemon(true);
			queueCleaner.start();
		} catch (NotFoundException e) {
			// this can't happen
			throw new AssertionError(e);
		}
	}
	
	/**
	 * Cleans up invoker instances when their object being invoked
	 * is eligible for garbage collection
	 * 
	 * @author jason
	 *
	 */
	private final class ListenerCleaner implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					Reference<?> reference = invokerInstanceQueue.remove();
					Invoker instance = cleanupMap.remove(reference);
					if (instance != null) {
						for (Set<Invoker> invokerSet : invokers.values()) {
							invokerSet.remove(instance);
						}
					}
				}
			} catch (Exception e) {
				// this shouldn't ever happen
				System.err.println("failure cleaning up references to event listeners!");
				e.printStackTrace();
			}
		}
	}
	
	private static final class MethodInfo {
		
		private final CtMethod method;
		private final String className;
		private final String parameterType;
		
		MethodInfo(final CtMethod method) throws Exception {
			
			this.method = method;
			className = method.getDeclaringClass().getPackageName() +
				".InvokerFor" +
				method.getDeclaringClass().getSimpleName() +
				"$" + method.getName() +
				"$" + Integer.toHexString(method.hashCode());
			// hanging onto event types is okay, they'll get reused
			parameterType = method.getParameterTypes()[0].getName();
		}
	}

	/**
	 * Wires event listeners to the publisher when an instance is being injected.
	 * @author jason
	 *
	 * @param <I>
	 */
	private final class EventWiringInjectionListener<I> implements InjectionListener<I> {
		@Override
		public void afterInjection(I injectee) {
			
			String name = injectee.getClass().getName();
			
			if (injectee instanceof EventManager) {
				// share state with the publisher
				((EventManager)injectee).listenerMap(invokers);
			} else if (subscribers.containsKey(name)) {
				// create an invoker class so we aren't doing this reflectively
				boolean cleanUp = false;
				for (MethodInfo invoked : subscribers.get(name)) {
					cleanUp = cleanUp || !invokerClasses.containsKey(invoked.className);
					try {
						
						Class<? extends Invoker> clazz = invokerClasses.containsKey(invoked.className) ?
							invokerClasses.get(invoked.className) :
							makeInvokerClass(invoked.className, injectee, invoked.method);
						
						WeakReference<Object> reference = new WeakReference<Object>(injectee, invokerInstanceQueue);
						Invoker invoker = clazz.getConstructor(WeakReference.class).newInstance(reference);
						cleanupMap.put(reference, invoker);
						invokers.get(Class.forName(invoked.parameterType)).add(invoker);
					} catch (Exception e) {
						throw new AssertionError(e);
					}
				}
				if (cleanUp) {
					subscribers.get(name).get(0).method.getDeclaringClass().detach();
				}
			}
		}

		@SuppressWarnings("unchecked")
		private Class<? extends Invoker> makeInvokerClass(String className, Object injectee, CtMethod invoked) throws Exception {
			// might be defined already
			try {
				return (Class<? extends Invoker>)Class.forName(className);
			} catch (ClassNotFoundException e) {}
			
			CtClass newClass = classPool.makeClass(className);
			newClass.addInterface(invokerClass);
			newClass.addField(CtField.make("private final java.lang.ref.WeakReference instance;", newClass));
			newClass.addConstructor(
				CtNewConstructor.make(
					new CtClass[] {classPool.get(WeakReference.class.getName())},
					null,
					"{this.instance = $1;}",
					newClass
				)
			);
			CtMethod newMethod = CtNewMethod.copy(invokeMethod, newClass, null);
			newMethod.setBody(
				"{" +
				injectee.getClass().getName() + " invokee = (" + injectee.getClass().getName() + ")instance.get();" +
				"if (invokee != null) invokee." + invoked.getName() + "((" + invoked.getParameterTypes()[0].getName() + ")$1);" +
				"}"
			);
			newClass.addMethod(newMethod);
			
			Class<? extends Invoker> invokerClass = classPool.toClass(
				newClass, 
				EventConfiguringTypeListener.class.getClassLoader(), 
				EventConfiguringTypeListener.class.getProtectionDomain()
			);
			
			invokerClasses.putIfAbsent(className, invokerClass);
			
			newClass.detach();
			
			return invokerClasses.get(className);
		}
	}

	@Override
	public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
		
		String name = type.toString();
		
		try {
			final CtClass clazz = classPool.get(type.toString());
			
			if (clazz.hasAnnotation(Subscriber.class)) {
				subscribers.put(name, new ArrayList<MethodInfo>());
				for (CtMethod method : clazz.getMethods()) {
					if (
						// should be not static, have one parameter
						// can return anything, but it's ignored
						method.hasAnnotation(Listener.class) &&
						!Modifier.isStatic(method.getModifiers()) &&
						method.getParameterTypes().length == 1
					) {
						subscribers.get(name).add(new MethodInfo(method));
						Class<?> key = Class.forName(method.getParameterTypes()[0].getName());
						if (!invokers.containsKey(key)) {
							invokers.putIfAbsent(key, Collections.newSetFromMap(PlatformDependent.<Invoker, Boolean>newConcurrentHashMap()));
						}
					}
				}
				
				if (subscribers.get(name).isEmpty()) {
					logger.warn("{} is a subscriber with no subscriptions", name);
					subscribers.remove(name);
					clazz.detach();
				}
			} else {
				clazz.detach();
			}
		} catch (NotFoundException nfe) {
			// generated classes won't make it into this class pool, which
			// really is as we want it for now
		} catch (Exception e) {
			throw new AssertionError(type.toString(), e);
		}
		
		encounter.register(new EventWiringInjectionListener<I>());
	}
}
