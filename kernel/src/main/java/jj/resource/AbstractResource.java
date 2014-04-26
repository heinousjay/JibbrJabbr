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

import javax.inject.Inject;

import jj.configuration.Location;

/**
 * internal helper for manipulating a resource.  ALL RESOURCES
 * MUST EXTEND THIS!
 * 
 * @author jason
 *
 */
public abstract class AbstractResource implements Resource {
	
	public static class Dependencies {
		
		protected final ResourceKey resourceKey;
		protected final Location base;
		
		@Inject
		public Dependencies(final ResourceKey resourceKey, final Location base) {
			this.resourceKey = resourceKey;
			this.base = base;
		}
	}

	protected static final Object[] EMPTY_ARGS = {};
	
	protected final ResourceKey cacheKey;
	
	protected final Location base;
	
	final Set<AbstractResource> dependents = new HashSet<>();
	volatile boolean alive = true;
	
	protected AbstractResource(final Dependencies dependencies) {
		this.cacheKey = dependencies.resourceKey;
		this.base = dependencies.base;
	}
	
	@ResourceThread
	public abstract boolean needsReplacing() throws IOException;

	@ResourceThread
	boolean isObselete() throws IOException {
		return !alive || needsReplacing();
	}
	
	/**
	 * return true from this method to be removed instead of reloaded on watch notifications 
	 */
	protected boolean removeOnReload() {
		return true;
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
	
	ResourceKey cacheKey() {
		return cacheKey;
	}
	
	@Override
	public Location base() {
		return base;
	}
	
	public String toString() {
		return getClass().getSimpleName() + "@" + uri();
	}
}
