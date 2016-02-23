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
package jj.resource;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Subscriber;

/**
 * Listens for ResourceLoaded events, and if they
 * match up with any of the resource waiting for the event,
 * it gets dispatched and that resource gets removed.  this
 * is an optimization to keep every single resource in the
 * system from listening for ResourceLoaded events.
 * @author jason
 *
 */
@Singleton
@Subscriber
class AbstractResourceEventDemuxer {
	
	AbstractResourceEventDemuxer() {}
	
	private final ConcurrentHashMap<ResourceIdentifier<?, ?>, AbstractResource<?>> waitingResources =
		new ConcurrentHashMap<>(16, 0.75f, 4);

	@Listener
	void on(ResourceLoaded event) {
		
		AbstractResource<?> resource = waitingResources.remove(event.identifier());
		if (resource != null) {
			resource.resourceLoaded();
		}
	}
	
	void awaitInitialization(AbstractResource<?> resource) {
		waitingResources.put(resource.identifier(), resource);
	}
}
