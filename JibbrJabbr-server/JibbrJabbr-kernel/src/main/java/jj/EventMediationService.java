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
package jj;

import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;
import static jj.KernelMessages.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.LinkedTransferQueue;

import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import jj.api.Blocking;
import jj.api.Event;
import jj.api.EventPublisher;
import jj.api.NonBlocking;

/**
 * maybe rework this so that internally one can instantiate a
 * known event object which automatically publishes it?
 * 
 * @author jason
 *
 */
@ThreadSafe
public class EventMediationService implements EventPublisher {
	
	@Immutable
	private static class RegistrationBundle {
		
		final Class<?> eventType;
		final Object instance;
		final Method m;
		final boolean add;
		final String key;
		
		RegistrationBundle(final Class<?> eventType, final Object instance, final Method m, final boolean add) {
			this.eventType = eventType;
			this.instance = instance;
			this.m = m;
			this.add = add;
			this.key = new StringBuffer()
				.append(System.identityHashCode(instance))
				.append("#")
				.append(m.getName())
				.toString();
		}
	}
	
	private final LinkedTransferQueue<RegistrationBundle> registrationQueue = new LinkedTransferQueue<>();
	private final LinkedTransferQueue<Object> publishQueue = new LinkedTransferQueue<>();
	private volatile boolean run = true;
	
	private final LocLogger logger;
	private final MessageConveyor messages;
	
	@NonBlocking
	public EventMediationService(
		final SynchThreadPool synchPool,
		final LocLogger logger,
		final MessageConveyor messages
	) {
		this.logger = logger;
		this.messages = messages;
		logger.info(ObjectInstantiated, EventMediationService.class);
		synchPool.submit(eventLoop);
		offerToLoop(this, true);
		offerToLoop(synchPool, true);
	}
	
	@NonBlocking
	private void offerToLoop(Object instance, boolean add) {
		for (Method m: instance.getClass().getMethods()) {
			Class<?> param;
			if ((m.getModifiers() & PUBLIC) == PUBLIC &&
				(m.getModifiers() & STATIC) != STATIC &&
				m.getReturnType() == Void.TYPE &&
				m.getParameterTypes().length == 1 &&
				(param = m.getParameterTypes()[0]).isAnnotationPresent(Event.class)) {
				registrationQueue.offer(new RegistrationBundle(param, instance, m, add));
			}
		}
	}
	
	/**
	 * Registers an object instance as an event listener.
	 * @param instance
	 */
	@NonBlocking
	public void register(Object instance) {
		assert (instance != null) : "cannot register a null for events";
		if (run) offerToLoop(instance, true);
	}
	
	/**
	 * Unregisters an object instance as an event listener.
	 * @param instance
	 */
	@NonBlocking
	public void unregister(Object instance) {
		assert (instance != null) : "cannot unregister a null for events";
		if (run) offerToLoop(instance, false);
	}
	
	/**
	 * Publishes an event
	 * @param event
	 */
	@NonBlocking
	public void publish(Object event) {
		assert (event != null) : "cannot publish a null event";
		assert (event.getClass().getAnnotation(Event.class) != null) : "cannot publish an object that is not an event";
		logger.trace("yay", event);
		if (run) publishQueue.offer(event);
	}
	
	/**
	 * Shuts the service down.  After this method is called, the instance is useless and
	 * should be discarded.
	 * 
	 * needs to be made pausable
	 */
	@NonBlocking
	public void control(KernelControl control) {
		run = (control == KernelControl.Start);
	}
	
	// should be weak so things can be collected?
	// need to investigate that.  for now count on
	// cleanup notifications
	// can only be accessed from inside the event loop
	private final HashMap<Class<?>, HashMap<String, RegistrationBundle>> listeners = new HashMap<>(); 
	
	
	private final Runnable eventLoop = new Runnable() {
		
		@Blocking
		@Override
		public void run() {
			String name = Thread.currentThread().getName();
			Thread.currentThread().setName(messages.getMessage(LoopThreadName, EventMediationService.class.getSimpleName()));
			try {

				while (run) {
					// get the event,
					// do the registration stuff
					// publish the event
					// OVER AND OVER FOREVER
					// or until shutdown
					
					// should we wake up every now and again so the registration queue doesn't get out of hand?
					// or combine the queues and wake up whenever there's something to do?
					Object event = publishQueue.take();
					
					if (run) { // if we've been shut down, just ignore it all
						HashMap<String, RegistrationBundle> h;
						RegistrationBundle i;
						while ((i = registrationQueue.poll()) != null) {
							
							if (i.add) {
								
								//EventMediationTransformer.makeClassBytes(i.eventType, i.instance.getClass(), i.m);
								
								// use ASM to convert the invocation into a non-reflective
								// runnable that takes the event as a construction parameter
								// and calls the method in its run method
								
								// use the instance class hashcode + '#' + method name as the key into a map of these tasks
								
								h = listeners.get(i.eventType);
								if (h == null) {
									h = new HashMap<>();
									listeners.put(i.eventType, h);
								}
								h.put(i.key, i);
							} else {
								h = listeners.get(i.eventType);
								if (h != null) {
									
									h.remove(i.key);
									
									if (h.isEmpty()) {
										listeners.remove(i.eventType);
									}
								}
							}
							
						}
						
						h = listeners.get(event.getClass());
						if (h != null) {
							for (RegistrationBundle i2 : h.values()) {
								try {
									// instead of direct invocation, should be a runnable that
									// gets put on the appropriate thread pool
									i2.m.invoke(i2.instance, event);
								// what do we do in exception cases?  egad
								} catch (IllegalAccessException e) {
									// can't happen.  if it does we've failed amazingly
								} catch (InvocationTargetException e) {
									e.getCause().printStackTrace();
								}
							}
						}
					}
				}
				
			} catch (InterruptedException ie) {
				run = false;
				Thread.currentThread().interrupt();
			}
			Thread.currentThread().setName(name);
		}
	};
}
