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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
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
	
	/**
	 * Cleans up invoker instances when their subject being invoked
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
				// this shouldn't happen
				System.err.println("failure cleaning up references to event listeners!");
				e.printStackTrace();
			}
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
			
			if (injectee instanceof EventManager) {
				// share state with the publisher
				((EventManager)injectee).listenerMap(invokers);
			} else if (subscribers.containsKey(injectee.getClass().getName())) {
				// create an invoker class so we aren't doing this reflectively
				for (CtMethod invoked : subscribers.get(injectee.getClass().getName())) {
					
					String className =
						invoked.getDeclaringClass().getPackageName() +
						".InvokerFor" +
							invoked.getDeclaringClass().getSimpleName() +
						"$" + invoked.getName() +
						"$" + Integer.toHexString(invoked.hashCode());
					
					try {
						
						Class<?> clazz = invokerClasses.containsKey(className) ?
							invokerClasses.get(className) :
							makeInvokerClass(className, injectee, invoked);
						
						WeakReference<Object> reference = new WeakReference<Object>(injectee, invokerInstanceQueue);
						Invoker invoker = (Invoker)clazz.getConstructor(WeakReference.class).newInstance(reference);
						cleanupMap.put(reference, invoker);
						invokers.get(Class.forName(invoked.getParameterTypes()[0].getName())).add(invoker);
					} catch (Exception e) {
						throw new AssertionError(e);
					}
				}
			}
		}
	}

	private final ConcurrentMap<String, List<CtMethod>> subscribers = PlatformDependent.newConcurrentHashMap();
	private final ConcurrentMap<String, Class<?>> invokerClasses = PlatformDependent.newConcurrentHashMap();
	private final ConcurrentMap<Class<?>, Set<Invoker>> invokers = PlatformDependent.newConcurrentHashMap();
	
	private final ConcurrentMap<WeakReference<Object>, Invoker> cleanupMap = PlatformDependent.newConcurrentHashMap();
	private final ReferenceQueue<Object> invokerInstanceQueue = new ReferenceQueue<>();
	
	private final ClassPool classPool = ClassPool.getDefault();
	private final CtClass invokerClass;
	private final CtMethod invokeMethod;
	
	EventConfiguringTypeListener() {
		try {
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

	@Override
	public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
		try {
			CtClass clazz = classPool.get(type.toString());
			
			if (clazz.hasAnnotation(Subscriber.class)) {
				subscribers.put(type.toString(), new ArrayList<CtMethod>());
				for (CtMethod method : clazz.getMethods()) {
					if (
						// should be not static, have one parameter
						// can return anything, but it's ignored
						method.hasAnnotation(Listener.class) &&
						!Modifier.isStatic(method.getModifiers()) &&
						method.getParameterTypes().length == 1
					) {
						subscribers.get(type.toString()).add(method);
						invokers.putIfAbsent(Class.forName(method.getParameterTypes()[0].getName()), new HashSet<Invoker>());
					}
				
				}
			}
		} catch (NotFoundException nfe) {
			// generated classes won't make it into this class pool, which
			// really is as we want it for now
		} catch (Exception e) {
			throw new AssertionError(type.toString(), e);
		}
		
		encounter.register(new EventWiringInjectionListener<I>());
	}

	private Class<?> makeInvokerClass(String className, Object injectee, CtMethod invoked) throws Exception {
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
		
		invokerClasses.putIfAbsent(className, newClass.toClass());
		return invokerClasses.get(className);
	}
}
