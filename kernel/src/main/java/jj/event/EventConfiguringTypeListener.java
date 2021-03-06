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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import jj.util.CodeGenHelper;

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
 * 
 * <p>
 * Breaking this up might pay some dividends?  it's pretty tricky to do so,
 * however.  Guice doesn't really have a reliable way of letting TypeListener
 * instances participate with the injector and still get all the notifications,
 * so it becomes a dance of figuring out how to manually instantiate stuff
 * which seems painful.  A quick google suggests this exact thing has been a
 * problem for others.
 * 
 * Possibly moving all of the management of invoker creation to a separate object
 * could work, if the creation of the structures can be rearranged so that the AOT
 * creation of the instance queues is just done at the point of injection rather
 * than the binding time.  That's the kind of sentence that will stop making
 * sense to me in the next hour.
 * @author jason
 *
 */
class EventConfiguringTypeListener implements TypeListener {

	/**
	 * mapped from subscriber class name -> the methodinfos for the listener methods of the class. 
	 * since this list stored as the value is only ever manipulated when a new type is encountered for
	 * the first time, and is read-only after that, it doesn't need to be synchronized
	 */
	private final ConcurrentHashMap<String, List<MethodInfo>> subscribers = new ConcurrentHashMap<>();
	
	/** 
	 * mapped from the class name of an invoker to the invoker class.
	 */
	private final ConcurrentHashMap<String, Class<? extends Invoker>> invokerClasses = new ConcurrentHashMap<>();
	
	/** 
	 * mapped from the event type -> the set of listener invokers for that event 
	 * the set stored as the value can be manipulated from multiple threads, so it needs
	 * to also be concurrent, but the value itself will only ever be set once on first encounter
	 * for a given event type and never removed after so that should do it
	 */
	private final ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<Invoker>> invokers = new ConcurrentHashMap<>();
	
	/** mapped from the weak reference to the instance invoked -> invoker */
	private final ConcurrentHashMap<WeakReference<Object>, Invoker> cleanupMap = new ConcurrentHashMap<>();
	
	private final ReferenceQueue<Object> invokerInstanceQueue = new ReferenceQueue<>();
	
	private final ClassPool classPool = CodeGenHelper.classPool();
	private final CtClass invokerClass;
	private final CtMethod invokeMethod;
	private final CtMethod targetMethod;
	private final CtClass weakReferenceClass;
	
	EventConfiguringTypeListener() {
		try {
			invokerClass = classPool.get(Invoker.class.getName());
			invokeMethod = invokerClass.getDeclaredMethod("invoke");
			targetMethod = invokerClass.getDeclaredMethod("target");
			weakReferenceClass = classPool.get(WeakReference.class.getName());
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
	private final class ListenerCleaner extends ServerTask {
		/**
		 * @param targetName
		 */
		public ListenerCleaner() {
			super("Event System cleanup");
		}

		@Override
		public void run() throws Exception {
			while (true) {
				Reference<?> reference = invokerInstanceQueue.remove();
				Invoker instance = cleanupMap.remove(reference);
				if (instance != null) {
					for (ConcurrentLinkedQueue<Invoker> invokerQueue : invokers.values()) {
						invokerQueue.remove(instance);
					}
				}
			}
		}
	}
	
	private static final class MethodInfo {
		
		private final Method method;
		private final String className;
		private final String parameterType;
		
		MethodInfo(final Method method) throws Exception {
			
			this.method = method;
			className = method.getDeclaringClass().getPackage().getName() +
				".InvokerFor" +
				method.getDeclaringClass().getSimpleName() +
				"$" + method.getName() + method.getParameterTypes()[0].getSimpleName() +
				"$" + Integer.toHexString(method.hashCode());
			// hanging onto event types is okay, they'll get reused
			parameterType = method.getParameterTypes()[0].getName();
		}
		
		@Override
		public String toString() {
			return method.toString();
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
		public void afterInjection(final I injectee) {
			if (injectee instanceof TaskRunner) {
				((TaskRunner)injectee).execute(new ListenerCleaner());
			} else if (injectee instanceof PublisherImpl) {
				// share state with the publisher - but it can't have events.  boo
				((PublisherImpl)injectee).listenerMap(invokers);
			} else {
				possiblySubscribeToEvents(injectee);
			}
		}
	}
	
	private void possiblySubscribeToEvents(final Object subscriber) {
		
		ArrayList<MethodInfo> methods = new ArrayList<>();
		
		Class<?> startingClass = subscriber.getClass();
		
		while (startingClass != Object.class) {
			String name = startingClass.getName();
			if (subscribers.containsKey(name)) {
				methods.addAll(subscribers.get(name));
			}
			
			startingClass = startingClass.getSuperclass();
		}

		for (final MethodInfo invoked : methods) {
			
			cleanupMap.computeIfAbsent(
				new WeakReference<Object>(subscriber, invokerInstanceQueue),
				reference -> {
					try {
						
						Invoker invoker = invokerClasses.computeIfAbsent(invoked.className, className -> {
							try {
								return makeInvokerClass(className, subscriber, invoked.method);
							} catch (Exception e) {
								throw new AssertionError(e);
							}
						}).getConstructor(WeakReference.class).newInstance(reference);
						
						invokers.get(Class.forName(invoked.parameterType)).offer(invoker);
						return invoker;
						
					} catch (Exception e) {
						throw new AssertionError(e);
					}
				}
			);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Invoker> makeInvokerClass(String className, Object injectee, Method invoked) throws Exception {
		// might be defined already
		try {
			return (Class<? extends Invoker>)Class.forName(className);
		} catch (ClassNotFoundException e) {}
		
		CtClass newClass = classPool.makeClass(className);
		newClass.addInterface(invokerClass);
		newClass.addField(CtField.make("private final java.lang.ref.WeakReference instance;", newClass));
		newClass.addConstructor(
			CtNewConstructor.make(
				new CtClass[] { weakReferenceClass },
				null,
				"{this.instance = $1;}",
				newClass
			)
		);
		
		final String invokedClassName = invoked.getDeclaringClass().getName();
		final String invokedMethodName = invoked.getName();
		final String eventClassName = invoked.getParameterTypes()[0].getName();
		
		CtMethod target = CtNewMethod.copy(targetMethod, newClass, null);
		target.setBody(
			"{" +
				"return \"" + invokedClassName + "." + invokedMethodName + "(" + eventClassName + ")\";" +
			"}"
		);
		newClass.addMethod(target);
		
		CtMethod invoke = CtNewMethod.copy(invokeMethod, newClass, null);
		invoke.setBody(
			"{" +
				invokedClassName + " invokee = (" + invokedClassName + ")instance.get();" +
				// if the invokee goes out of scope, we can stop sending events to it
				"if (invokee != null) invokee." + invokedMethodName + "((" + eventClassName + ")$1);" +
			"}"
		);
		newClass.addMethod(invoke);
		
		Class<? extends Invoker> invokerClass = classPool.toClass(
			newClass, 
			getClass().getClassLoader(), 
			getClass().getProtectionDomain()
		);
		
		newClass.detach();
		
		return invokerClass;
	}
	
	private List<Method> resolveAllListenerMethods(Class<?> c) {
		List<Method> result = new ArrayList<>();
		do {
			for (Method m : c.getDeclaredMethods()) {
				if ((m.getAnnotation(Listener.class) != null) &&
					!Modifier.isStatic(m.getModifiers()) &&
					!Modifier.isPrivate(m.getModifiers()) &&
					m.getParameterTypes().length == 1)
				{
					result.add(m);
				}
			}
			
		} while ((c = c.getSuperclass()) != Object.class);
		
		return result;
	}

	@Override
	public <I> void hear(TypeLiteral<I> type, final TypeEncounter<I> encounter) {
		// anything we heard here, we also want to wire
		encounter.register(new EventWiringInjectionListener<I>());
		
		final Class<?> clazz = type.getRawType();
		
		if (!TaskRunner.class.isAssignableFrom(clazz) && !Publisher.class.isAssignableFrom(clazz)) {
		
			String name = type.toString();
			
			subscribers.computeIfAbsent(name, className -> {
				
				ArrayList<MethodInfo> result = new ArrayList<>();
				try {

					for (Method method : resolveAllListenerMethods(clazz)) {
						
						result.add(new MethodInfo(method));

						invokers.computeIfAbsent(
							Class.forName(method.getParameterTypes()[0].getName()),
							a -> new ConcurrentLinkedQueue<>()
						);
					}
				} catch (NotFoundException nfe) {
					encounter.addError("unable to load " + className + " for subscription");
				} catch (Exception e) {
					encounter.addError(e);
				}

				if (result.isEmpty()) {
					encounter.addError("%s is annotated as a @Subscriber but has no @Listener methods", className);
				}
				
				return Collections.unmodifiableList(result);
			});
		}
	}
}
