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

import java.io.IOException;

import jj.execution.IOThread;

/**
 * internal helper for manipulating a resource.  ALL RESOURCES
 * MUST EXTEND THIS!
 * 
 * @author jason
 *
 */
abstract class AbstractResource implements Resource {
	
	private final ResourceCacheKey cacheKey;
	
	AbstractResource(final ResourceCacheKey cacheKey) {
		this.cacheKey = cacheKey;
	}
	
	@IOThread
	abstract boolean needsReplacing() throws IOException;
	
	/**
	 * Register a dependency on another resource.  This means that
	 * when an update to a dependency is detected, the dependent will
	 * be rebuilt, even if it has no changes of its own
	 * 
	 * @param dependency
	 */
	public void dependsOn(Resource dependency) {
		
	}
}
