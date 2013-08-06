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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	private final Set<AbstractResource> dependents = new HashSet<>();
	private volatile boolean obselete = false;
	
	AbstractResource(final ResourceCacheKey cacheKey) {
		this.cacheKey = cacheKey;
	}
	
	@IOThread
	abstract boolean needsReplacing() throws IOException;
	
	final boolean isObselete() throws IOException {
		return obselete || needsReplacing();
	}
	
	final void markObselete() {
		obselete = true;
	}
	
	/**
	 * the arguments used to create this resource
	 * @return
	 */
	abstract Object[] creationArgs();
	
	@Override
	public void dependsOn(Resource dependency) {
		((AbstractResource)dependency).dependents.add(this);
	}
	
	Set<AbstractResource> dependents() {
		return Collections.unmodifiableSet(dependents);
	}
	
	ResourceCacheKey cacheKey() {
		return cacheKey;
	}
}
