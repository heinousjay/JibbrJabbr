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

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Inject the Publisher to publish events.
 * 
 * @author jason
 *
 */
@Singleton
class EventManager implements Publisher {
	
	protected final AtomicInteger count = new AtomicInteger();
	
	private Map<Class<?>, LinkedBlockingQueue<Invoker>> listenerMap;
	
	@Inject
	EventManager() {}
	
	void listenerMap(Map<Class<?>, LinkedBlockingQueue<Invoker>> listenerMap) {
		this.listenerMap = listenerMap;
	}
	
	private void invoke(final Object event, final Class<?> clazz) {
		if (clazz != null && clazz != Object.class) {
			if (listenerMap.containsKey(clazz)) {
				for (Invoker invoker : listenerMap.get(clazz)) {
					try {
						invoker.invoke(event);
					} catch (Exception e) {
						throw new AssertionError("broken event listener! " + invoker.getClass().getName(), e);
					}
				}
			}
			for (Class<?> iface : clazz.getInterfaces()) {
				invoke(event, iface);
			}
			invoke(event, clazz.getSuperclass());
		}
	}
	
	@Override
	public void publish(final Object event) {
		count.getAndIncrement();
		invoke(event, event.getClass());
	}
}
