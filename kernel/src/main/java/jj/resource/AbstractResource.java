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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import jj.configuration.Location;
import jj.event.Publisher;

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
		protected final Publisher publisher;
		
		@Inject
		public Dependencies(final ResourceKey resourceKey, final Location base, final Publisher publisher) {
			this.resourceKey = resourceKey;
			this.base = base;
			this.publisher = publisher;
		}
	}

	protected static final Object[] EMPTY_ARGS = {};
	
	protected final ResourceKey cacheKey;
	
	protected final Location base;
	
	protected final Publisher publisher;
	
	final Set<AbstractResource> dependents = new HashSet<>();
	final AtomicBoolean alive = new AtomicBoolean(true);
	
	protected AbstractResource(final Dependencies dependencies) {
		this.cacheKey = dependencies.resourceKey;
		this.base = dependencies.base;
		this.publisher = dependencies.publisher;
	}
	
	@ResourceThread
	public abstract boolean needsReplacing() throws IOException;

	@ResourceThread
	boolean isObselete() throws IOException {
		return !alive.get() || needsReplacing();
	}
	
	/**
	 * return true from this method to be removed instead of reloaded on watch notifications 
	 */
	protected boolean removeOnReload() {
		return true;
	}
	
	@Override
	public void addDependent(Resource dependent) {
		assert alive.get() : "cannot accept dependents, i am dead " + toString();
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
		return alive.get();
	}
	
	void kill() {
		if (alive.getAndSet(false)) {
			publisher.publish(new ResourceKilled(getClass(), base(), name(), creationArgs()));
		}
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
