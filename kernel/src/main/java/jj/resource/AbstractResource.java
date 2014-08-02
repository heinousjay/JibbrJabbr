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
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.mozilla.javascript.Scriptable;

import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.util.Clock;

/**
 * internal helper for manipulating a resource.  ALL RESOURCES
 * MUST EXTEND THIS!
 * 
 * @author jason
 *
 */
@Subscriber
public abstract class AbstractResource implements Resource {
	
	public static class Dependencies {
		
		protected final Clock clock;
		protected final ResourceConfiguration resourceConfiguration;
		protected final ResourceKey resourceKey;
		protected final Location base;
		protected final Publisher publisher;
		protected final ResourceFinder resourceFinder;
		
		@Inject
		public Dependencies(
			final Clock clock,
			final ResourceConfiguration resourceConfiguration,
			final ResourceKey resourceKey,
			final Location base,
			final Publisher publisher,
			final ResourceFinder resourceFinder
		) {
			this.clock = clock;
			this.resourceConfiguration = resourceConfiguration;
			this.resourceKey = resourceKey;
			this.base = base;
			this.publisher = publisher;
			this.resourceFinder = resourceFinder;
		}
	}

	protected static final Object[] EMPTY_ARGS = {};
	
	protected final ResourceKey cacheKey;
	
	protected final long creationTime;
	
	protected final Location base;
	
	protected final Publisher publisher;
	
	protected final ResourceFinder resourceFinder;
	
	private final ConcurrentHashMap<ResourceKey, AbstractResource> dependents = new ConcurrentHashMap<>(2, 0.75f, 2);
	
	private final AtomicBoolean alive = new AtomicBoolean(true);
	
	protected AbstractResource(final Dependencies dependencies) {
		this.creationTime = dependencies.clock.time();
		this.cacheKey = dependencies.resourceKey;
		this.base = dependencies.base;
		this.publisher = dependencies.publisher;
		this.resourceFinder = dependencies.resourceFinder;
	}
	
	@Listener
	void resourceLoaded(ResourceLoaded event) {
		// little bit of internal magic here - post initialization of either a
		// FileResource or a DirectoryResource rooted in Base should be added to
		// the directory structure.
		if (
			(this instanceof ParentedResource) && 
			this.base().parentInDirectory() &&
			event.matches(this)
		) {
			String parentName = name().substring(0, name().lastIndexOf('/') + 1);
			if (!parentName.equals(name())) {
				DirectoryResource parent = resourceFinder.findResource(DirectoryResource.class, base(), parentName);
				// possibly the best bet in this case is to make the missing directory,
				// which really oughta go into another thread? cause this could get all deadlocky
				assert parent != null : "no parent directory for " + this;
				parent.addDependent(this);
			}
		}
	}
	
	@ResourceThread
	public abstract boolean needsReplacing() throws IOException;

	@ResourceThread
	boolean isObselete() throws IOException {
		return !alive.get() || needsReplacing();
	}
	
	/**
	 * return true from this method to be removed instead of reloaded on watch notifications,
	 * or return false to be reloaded in the background and replaced.  default is true
	 */
	protected boolean removeOnReload() {
		return true;
	}
	
	@Override
	public void addDependent(Resource dependent) {
		assert alive.get() : "cannot accept dependents, i am dead " + toString();
		assert dependent != null : "can not depend on null";
		assert dependent != this : "can not depend on myself";
		AbstractResource r = (AbstractResource)dependent;
		dependents.put(r.cacheKey(), r);
	}
	
	@Listener
	void resourceKilled(ResourceKilled event) {
		dependents.remove(event.resourceKey);
	}

	/**
	 * the arguments used to create this resource. mocking needs prevent this
	 * from being kept package private but don't call it
	 * @return
	 */
	protected Object[] creationArgs() {
		return EMPTY_ARGS;
	}
	
	Collection<AbstractResource> dependents() {
		return Collections.unmodifiableCollection(dependents.values());
	}
	
	void describe(Scriptable to) {
		// put the basics in place, then start calling
		// downstream to add more?  sure
		// certain stuff can be done here anyway?
		// ugh. lee.
		
		// don't forget to convert to strings if needed
		to.put("type",         to, getClass().getName());
		to.put("name",         to, name());
		to.put("base",         to, base().toString());
		to.put("uri",          to, uri());
		to.put("sha1",         to, sha1());
		to.put("creationTime", to, creationTime);
	}
	
	public boolean alive() {
		return alive.get();
	}
	
	void kill() {
		if (alive.getAndSet(false)) {
			publisher.publish(new ResourceKilled(this));
		}
	}
	
	@Override
	public ResourceKey cacheKey() {
		return cacheKey;
	}
	
	@Override
	public Location base() {
		return base;
	}
	
	public String toString() {
		return getClass().getName() + "@" + base() + "/" + name();
	}
}
