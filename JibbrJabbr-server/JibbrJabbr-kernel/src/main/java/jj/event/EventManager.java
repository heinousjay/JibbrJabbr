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

import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
class EventManager implements Publisher {
	
	private Map<Class<?>, Set<Invoker>> listenerMap;
	
	void listenerMap(Map<Class<?>, Set<Invoker>> listenerMap) {
		this.listenerMap = listenerMap;
	}
	
	@Override
	public void publish(final Object event) {
		for (Class<?> clazz : listenerMap.keySet()) {
			if (clazz.isAssignableFrom(event.getClass())) {
				for (Invoker invoker : listenerMap.get(clazz)) {
					invoker.invoke(event);
				}
			}
		}
	}

}
