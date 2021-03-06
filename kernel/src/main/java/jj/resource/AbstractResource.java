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
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.logging.LoggedEvent;
import org.mozilla.javascript.Scriptable;

import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import org.slf4j.Logger;

/**
 * <p>
 * basic {@link Resource} behavior.  ALL RESOURCES MUST EXTEND THIS. the binders will not
 * bind classes that do not.
 * 
 * <p>
 * Provides all of the interesting hooks into the system, as well
 * as default implementations of most of the core {@link Resource}
 * interface, excluding {@link Resource#sha1()}
 * 
 * @author jason
 *
 */
@Subscriber
public abstract class AbstractResource<T> implements Resource<T> {
	
	@Singleton
	public static class AbstractResourceDependencies {

		protected final Clock clock;
		protected final ResourceConfiguration resourceConfiguration;
		protected final AbstractResourceEventDemuxer demuxer;
		protected final Publisher publisher;
		protected final ResourceFinder resourceFinder;
		
		@Inject
		protected AbstractResourceDependencies(
			Clock clock,
			ResourceConfiguration resourceConfiguration,
			AbstractResourceEventDemuxer demuxer,
			Publisher publisher,
			ResourceFinder resourceFinder
		) {
			this.clock = clock;
			this.resourceConfiguration = resourceConfiguration;
			this.demuxer = demuxer;
			this.publisher = publisher;
			this.resourceFinder = resourceFinder;
		}
		
	}
	
	/**
	 * technique used to bundle up all of the dependencies for the base
	 * class, to avoid changing the constructor signature for descendants
	 * all the time. public so it can be overridden, as the script system
	 * does.
	 * 
	 * @author jason
	 *
	 */
	public static class Dependencies {
		
		protected final AbstractResourceDependencies abstractResourceDependencies;
		protected final ResourceIdentifier<?, ?> identifier;
		
		@Inject
		protected Dependencies(
			AbstractResourceDependencies abstractResourceDependencies,
			ResourceIdentifier<?, ?> identifier
		) {
			this.abstractResourceDependencies = abstractResourceDependencies;
			this.identifier = identifier;
		}
	}
	
	/**
	 * The key that identifies this resource in the cache
	 */
	protected final ResourceIdentifier<? extends Resource<T>, T> identifier;
	
	/**
	 * When this resource was created according to the system {@link Clock}
	 */
	protected final long creationTime;
	
	protected final Publisher publisher;
	
	protected final ResourceFinder resourceFinder;
	
	/**
	 * The configuration of the resource system
	 */
	protected final ResourceConfiguration resourceConfiguration;

	/**
	 * The resolved settings for this resource
	 */
	protected final ResourceSettings settings;
	
	private final ConcurrentHashMap<ResourceIdentifier<?, ?>, AbstractResource<?>> dependents =
		new ConcurrentHashMap<>(2, 0.75f, 2);
	
	private final AtomicBoolean alive = new AtomicBoolean(true);

	@SuppressWarnings("unchecked")
	private ResourceIdentifier<? extends Resource<T>, T> cast(ResourceIdentifier<?, ?> identifier) {
		return (ResourceIdentifier<? extends Resource<T>, T>)identifier;
	}
	
	protected AbstractResource(Dependencies dependencies) {
		this.identifier = cast(dependencies.identifier);
		this.creationTime = dependencies.abstractResourceDependencies.clock.millis();
		this.publisher = dependencies.abstractResourceDependencies.publisher;
		this.resourceFinder = dependencies.abstractResourceDependencies.resourceFinder;
		this.resourceConfiguration = dependencies.abstractResourceDependencies.resourceConfiguration;

		if ((this instanceof FileSystemResource) && this.base().parentInDirectory()) {
			dependencies.abstractResourceDependencies.demuxer.awaitInitialization(this);
		}
		
		ResourceSettings baseSettings = resourceConfiguration.fileTypeSettings().get(extension());
		if (baseSettings == null) {
			baseSettings = resourceConfiguration.defaultSettings();
		}
		
		// and specific based on the type/base/name?  need to figure out how to represent that tuple.  maybe just name?
		
		settings = baseSettings;
	}

	void resourceLoaded() {
		// little bit of internal magic here - post initialization of either a
		// FileResource or a DirectoryResource rooted in Base should be added to
		// the directory structure.
		int index = name().lastIndexOf('/');
		String parentName = index == -1 ?
			(name().equals("") ? null : "") :
			name().substring(0, index);

		if (parentName != null) {
			DirectoryResource parent = resourceFinder.findResource(DirectoryResource.class, base(), parentName);
			// possibly the best bet in this case is to make the missing directory,
			// which really oughta go into another thread? cause this could get all deadlocky
			if (parent == null) {
				throw new AssertionError(
					"no parent directory " + parentName +
					" for " + this +
					", looked in " + base() + " for " + parentName
				);
			}
			parent.addDependent(this);
		}
	}
	
	/**
	 * handles cleaning up dependency tracking when resources are killed. since that only happens from the watch thread,
	 * and only happens when there are modifications to resources, which are both rare events in a sense (and things
	 * that should never matter in a production setting) then having this listener live in every resource in the system
	 * seems like a reasonable thing.  Note that the loaded event is demuxed in a separate component because that event
	 * is going to be thrown around like crazy and will cause resource creation to slow as more resources are created.
	 * 
	 * @param event the event
	 */
	@Listener
	void on(ResourceKilled event) {
		dependents.remove(event.identifier());
	}
	
	/**
	 * Override this implementation to provide a specific extension, used to determine base settings.
	 * AbstractFileResource provides an implementation that suffices in most cases.
	 */
	protected String extension() {
		return "";
	}
	
	/**
	 * A resource-specific test to indicate if the given resource should be replaced when the
	 * watch system becomes aware of it. any sort of check is allowed at this point. this method is
	 * only used if the resource is still considered "alive" so if the resource has been killed by
	 * some other method (such as dependency propagation) this has no effect
	 * @return true to be replaced
	 * @throws IOException
	 */
	@ResourceThread
	public abstract boolean needsReplacing() throws IOException;
	
	/**
	 * return true from this method to be removed instead of reloaded on watch notifications,
	 * or return false to be reloaded in the background and replaced.  default is true
	 */
	protected boolean removeOnReload() {
		return true;
	}
	
	@Override
	public void addDependent(Resource<?> dependent) {
		assert alive.get() : "cannot accept dependents, i am dead " + toString();
		assert dependent != null : "can not depend on null";
		assert dependent != this : "can not depend on myself";
		assert dependent.alive() : "can not depend on dead resources";
		publisher.publish(new DependentAdded(toString(), dependent.toString()));
		dependents.put(dependent.identifier(), (AbstractResource<?>)dependent);
	}

	@ResourceLogger
	private static class DependentAdded extends LoggedEvent {

		private final String parentString;
		private final String dependentString;

		DependentAdded(String parentString, String dependentString) {
			this.parentString = parentString;
			this.dependentString = dependentString;
		}

		@Override
		public void describeTo(Logger logger) {
			logger.debug("{} is depending on {}", dependentString, parentString);
		}
	}
	
	/**
	 * retrieve an unmodifiable collection of this resource's dependents
	 */
	Collection<AbstractResource<?>> dependents() {
		return Collections.unmodifiableCollection(dependents.values());
	}
	
	/**
	 * DO NOT LIKE THIS HERE!
	 * @param to the scriptable to fill with a description
	 */
	void describe(Scriptable to) {
		// put the basics in place, then start calling
		// downstream to add more?  sure
		// certain stuff can be done here anyway?
		// ugh. lee.
		
		// don't forget to convert to strings if needed
		// need an id! use the resource key?
		to.put("id",           to, identifier.toString());
		to.put("type",         to, getClass().getName());
		to.put("name",         to, name());
		to.put("base",         to, base().toString());
		to.put("uri",          to, uri());
		to.put("sha1",         to, sha1());
		to.put("creationTime", to, creationTime);
	}
	
	/**
	 * flag tracking if this resource is considered alive. when this method begins returning
	 * false, the resource has been taken out of service.
	 */
	public boolean alive() {
		return alive.get();
	}
	
	/**
	 * kills this resource.
	 */
	void kill() {
		if (alive.getAndSet(false)) {
			publisher.publish(new ResourceKilled(this));
			died();
		}
	}
	
	/**
	 * internal notification that this resource has died, so some descendant class can
	 * make something of the information. the base implementation does nothing
	 */
	protected void died() {
		// mainly to allow descendants to publish their own death event
	}
	
	@Override
	public Charset charset() {
		return null; // by default, resources don't have one at all.  might not be text!
	}

	@Override
	public ResourceIdentifier<? extends Resource<T>, T> identifier() {
		return identifier;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Resource<T>> type() {
		return (Class<? extends Resource<T>>)getClass();
	}
	
	@Override
	public Location base() {
		return identifier.base;
	}
	
	@Override
	public String name() {
		return identifier.name;
	}

	@Override
	public T creationArg() {
		return identifier.argument;
	}

	@Override
	public final URI uri() {
		return null;
	}

	public String toString() {
		return identifier.toString();
	}

	// equals and hashCode are left as Object impls deliberately
}
