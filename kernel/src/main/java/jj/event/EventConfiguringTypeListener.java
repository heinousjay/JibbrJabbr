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

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8.Fun;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import jj.util.ClassPoolHelper;

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
	 * mapped from subscriber class name -> the methodinfos for the listener methods of the class. 
	 * since this list stored as the value is only ever manipulated when a new type is encountered for
	 * the first time, and is read-only after that, it doesn't need to be synchronized
	 */
	private final ConcurrentHashMapV8<String, List<MethodInfo>> subscribers = new ConcurrentHashMapV8<>();
	
	/** 
	 * mapped from the class name of an invoker to the invoker class.  wait, what? 
	 * well it doesn't just rely on class.forname because it needs to keep track of
	 * which classes have already been generated so the CtClass objects can be
	 * cleaned up
	 */
	private final ConcurrentHashMapV8<String, Class<? extends Invoker>> invokerClasses = new ConcurrentHashMapV8<>();
	
	/** 
	 * mapped from the event type -> the set of listener invokers for that event 
	 * the set stored as the value can be manipulated from multiple threads, so it needs
	 * to also be concurrent, but the value itself will only ever be set once on first encounter
	 * for a given event type and never removed after so that should do it
	 */
	private final ConcurrentHashMapV8<Class<?>, LinkedBlockingQueue<Invoker>> invokers = new ConcurrentHashMapV8<>();
	
	/** mapped from the weak reference to the instance invoked -> invoker */
	private final ConcurrentHashMapV8<WeakReference<Object>, Invoker> cleanupMap = new ConcurrentHashMapV8<>();
	
	private final ReferenceQueue<Object> invokerInstanceQueue = new ReferenceQueue<>();
	
	private final ClassPool classPool;
	private final CtClass invokerClass;
	private final CtMethod invokeMethod;
	
	EventConfiguringTypeListener() {
		try {
			classPool = new ClassPool();
			classPool.appendClassPath(new LoaderClassPath(ClassPoolHelper.class.getClassLoader()));
			invokerClass = classPool.get("jj.event.Invoker");
			invokeMethod = invokerClass.getDeclaredMethod("invoke");
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
		 * @param name
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
					for (LinkedBlockingQueue<Invoker> invokerQueue : invokers.values()) {
						invokerQueue.remove(instance);
					}
				}
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
			
			final String name = injectee.getClass().getName();

			if (injectee instanceof TaskRunner) {
				((TaskRunner)injectee).execute(new ListenerCleaner());
			}
			
			if (injectee instanceof EventManager) {
				// share state with the publisher - but it can't have events.  boo
				((EventManager)injectee).listenerMap(invokers);
			} else if (subscribers.containsKey(name)) {

				for (final MethodInfo invoked : subscribers.get(name)) {
					
					cleanupMap.computeIfAbsent(
						new WeakReference<Object>(injectee, invokerInstanceQueue),
						new Fun<WeakReference<Object>, Invoker>() {

						@Override
						public Invoker apply(WeakReference<Object> reference) {
							try {
								invokerClasses.computeIfAbsent(invoked.className, new Fun<String, Class<? extends Invoker>>() {

									@Override
									public Class<? extends Invoker> apply(String className) {
										try {
											return makeInvokerClass(className, injectee, invoked.method);
										} catch (Exception e) {
											throw new AssertionError(e);
										}
									}
								});
								
								Invoker invoker = invokerClasses.get(invoked.className).getConstructor(WeakReference.class).newInstance(reference);
								invokers.get(Class.forName(invoked.parameterType)).offer(invoker);
								return invoker;
							} catch (Exception e) {
								throw new AssertionError(e);
							}
						}
					});
					
					
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
				getClass().getClassLoader(), 
				getClass().getProtectionDomain()
			);
			
			newClass.detach();
			
			return invokerClass;
		}
	}

	@Override
	public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
		
		String name = type.toString();

		try {
			final CtClass clazz = classPool.get(type.toString());
		
			if (clazz.hasAnnotation(Subscriber.class)) {
				
				subscribers.computeIfAbsent(name, new Fun<String, List<MethodInfo>>() {
	
					@Override
					public List<MethodInfo> apply(String name) {
						ArrayList<MethodInfo> result = new ArrayList<>();
						
						try {
							for (CtMethod method : clazz.getMethods()) {
								if (
									// should be not static, have one parameter
									// can return anything, but it's ignored
									method.hasAnnotation(Listener.class) &&
									!Modifier.isStatic(method.getModifiers()) &&
									method.getParameterTypes().length == 1
								) {
									result.add(new MethodInfo(method));
									
									invokers.computeIfAbsent(
										Class.forName(method.getParameterTypes()[0].getName()),
										new Fun<Class<?>, LinkedBlockingQueue<Invoker>>() {
		
											@Override
											public LinkedBlockingQueue<Invoker>apply(Class<?> a) {
												return new LinkedBlockingQueue<>();
											}
										}
									);
								}
							}
						} catch (Exception e) {
							throw new AssertionError(type.toString(), e);
						}
						
						return Collections.unmodifiableList(result);
					}
				});
				
				
				assert !subscribers.get(name).isEmpty() : name + " is subscribing but has no listeners";
				
			} else {
				clazz.detach();
			}
		} catch (NotFoundException e1) {
			// don't care
		}
		
		encounter.register(new EventWiringInjectionListener<I>());
	}
}
