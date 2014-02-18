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
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.logging.EmergencyLogger;

import org.slf4j.Logger;

/**
 * Inject the Publisher to publish events.
 * 
 * @author jason
 *
 */
@Singleton
class EventManager implements Publisher {
	
	private final Logger logger;
	
	private Map<Class<?>, Set<Invoker>> listenerMap;
	
	@Inject
	EventManager(final @EmergencyLogger Logger logger) {
		this.logger = logger;
	}
	
	void listenerMap(Map<Class<?>, Set<Invoker>> listenerMap) {
		this.listenerMap = listenerMap;
	}
	
	// TODO - should this be a ServerTask?
	// i think yes
	private void invoke(final Object event, final Class<?> clazz) {
		if (clazz != null && clazz != Object.class) {
			if (listenerMap.containsKey(clazz)) {
				for (Invoker invoker : listenerMap.get(clazz)) {
					try {
						invoker.invoke(event);
					} catch (Exception e) {
						logger.error("exception invoking event listener", e);
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
		invoke(event, event.getClass());
	}
}
