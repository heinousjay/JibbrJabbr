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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandle;

/**
 * Inject the {@link Publisher} to publish events.
 * 
 * @author jason
 *
 */
@Singleton
class PublisherImpl implements Publisher {

	private static class BadEventListenerError extends AssertionError {
		BadEventListenerError(MethodHandle badHandle, Throwable cause) {
			super(
				"broken event listener in " +
					badHandle.type().parameterType(0).getName() +
					" receiving " +
					badHandle.type().parameterType(1).getName(),
				cause
			);
		}
	}

	private final EventSystemState state;

	@Inject
	PublisherImpl(EventSystemState state) {
		this.state = state;
	}

	private void invoke(final Object event, final Class<?> eventType) {

		if (eventType != null && eventType != Object.class) {

			state.handleByReceiverTypesFor(eventType).forEach(
				(receiverType, handle) ->
					state.instancesFor(receiverType).forEach(
						ref -> {
							Object instance = ref.get();
							if (instance != null) {
								try {
									handle.invoke(instance, event);
								} catch (Throwable cause) {
									throw new BadEventListenerError(handle, cause);
								}
							}
						}
					)
			);

			// recurse up the class hierarchy of the event to get wider listeners
			for (Class<?> iface : eventType.getInterfaces()) {
				invoke(event, iface);
			}
			invoke(event, eventType.getSuperclass());
		}

	}

	@Override
	public void publish(final Object event) {
		assert event != null : "Can't publish nothing!";
		assert event.getClass() != Object.class : "Can't publish an Object!";
		invoke(event, event.getClass());
	}
}
