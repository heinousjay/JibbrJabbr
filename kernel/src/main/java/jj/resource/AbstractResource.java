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
public abstract class AbstractResource implements Resource {

	protected static final Object[] EMPTY_ARGS = {};
	
	private final ResourceCacheKey cacheKey;
	final Set<AbstractResource> dependents = new HashSet<>();
	volatile boolean alive = true;
	
	protected AbstractResource(final ResourceCacheKey cacheKey) {
		this.cacheKey = cacheKey;
	}
	
	@IOThread
	public abstract boolean needsReplacing() throws IOException;

	@IOThread
	boolean isObselete() throws IOException {
		return !alive || needsReplacing();
	}
	
	@Override
	public void addDependent(Resource dependent) {
		assert alive : "cannot accept dependents, i am dead " + toString();
		assert dependent != null : "can not depend on null";
		assert dependent != this : "can not depend on myself";
		dependents.add((AbstractResource)dependent);
	}

	
	/**
	 * the arguments used to create this resource. mocking needs prevent this
	 * from being kept package private but don't call it
	 * @return
	 */
	protected Object[] creationArgs() {
		return EMPTY_ARGS;
	}
	
	Set<AbstractResource> dependents() {
		return Collections.unmodifiableSet(dependents);
	}
	
	public boolean alive() {
		return alive;
	}
	
	void kill() {
		alive = false;
	}
	
	ResourceCacheKey cacheKey() {
		return cacheKey;
	}
	
	public String toString() {
		return getClass().getSimpleName() + "@" + uri();
	}
}
