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

/**
 * The event published when a resource is retired from the cache.
 * 
 * @author jason
 *
 */
public class ResourceKilled extends ResourceEvent implements ResourceCacheUpdated {

	/**
	 * @param resourceClass
	 * @param base
	 * @param name
	 * @param arguments
	 */
	ResourceKilled(AbstractResource<?> resource) {
		super(resource);
	}

	@Override
	protected String description() {
		return "resource killed";
	}

}
