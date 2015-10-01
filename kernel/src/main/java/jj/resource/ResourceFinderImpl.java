package jj.resource;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.execution.CurrentTask;
import jj.logging.Warning;
import jj.util.Closer;

/**
 * coordinates access to the resource cache for the outside
 * world.
 * @author jason
 *
 */
@Singleton
class ResourceFinderImpl implements ResourceFinder {
	
	private final ConcurrentMap<ResourceIdentifier<?, ?>, ResourceTask> resourcesInProgress =
		new ConcurrentHashMap<>(16, 0.75f, 4);

	private final ResourceCache resourceCache;
	
	private final Publisher publisher;
	
	private final CurrentTask currentTask;

	private final ResourceIdentifierMaker resourceIdentifierMaker;
	
	@Inject
	ResourceFinderImpl(
		ResourceCache resourceCache,
		Publisher publisher,
		CurrentTask currentTask,
	    ResourceIdentifierMaker resourceIdentifierMaker
	) {
		this.resourceCache = resourceCache;
		this.publisher = publisher;
		this.currentTask = currentTask;
		this.resourceIdentifierMaker = resourceIdentifierMaker;
	}
	
	@Override
	public <T extends Resource<Void>> T findResource(Class<T> resourceClass, Location location, String name) {
		return findResource(resourceIdentifierMaker.make(resourceClass, location, name, null));
	}
	
	@Override
	public <T extends Resource<A>, A> T findResource(Class<T> resourceClass, Location location, String name, A argument) {
		return findResource(resourceIdentifierMaker.make(resourceClass, location, name, argument));
	}

	@Override
	public <T extends Resource<A>, A> T findResource(ResourceIdentifier<T, A> identifier) {
		return resourceIdentifiers(identifier).stream()
			.map(resourceCache::get)
			.filter(r -> r != null)
			.findFirst()
			.orElse(null);
	}

	@Override
	@ResourceThread
	public <T extends Resource<Void>> T loadResource(Class<T> resourceClass, Location location, String name) {
		return loadResource(resourceIdentifierMaker.make(resourceClass, location, name, null));
	}
	
	@Override
	@ResourceThread
	public <T extends Resource<A>, A> T loadResource(Class<T> resourceClass, Location location, String name, A argument) {
		return loadResource(resourceIdentifierMaker.make(resourceClass, location, name, argument));
	}

	@Override
	@ResourceThread
	public <T extends Resource<A>, A> T loadResource(ResourceIdentifier<T, A> identifier) {

		assert currentTask.currentIs(ResourceTask.class) : "Can only call loadResource from a ResourceTask";

		try (Closer ignored = acquire(identifier)) {

			T resource = findResource(identifier);
			if (resource == null) {
				resource = resourceIdentifiers(identifier).stream()
					.map(this::createResource)
					.filter(r -> r != null)
					.findFirst()
					.orElse(null);
			}

			return resource;

		} catch (InterruptedException ie) {
			// this is a shutdown
		}

		return null;
	}

	private <T extends Resource<A>, A> T createResource(ResourceIdentifier<T, A> identifier) {
		ResourceCreator<A, T> creator = findCreator(identifier);

		try {
			T resource = creator.create(identifier.base, identifier.name, identifier.argument);

			// resource not found!
			if (resource == null) {
				publisher.publish(new ResourceNotFound(identifier));
				// try to add it to the cache.  shouldn't be in there!
				// if it isn't, tell the world we loaded a new resource
			} else if (resourceCache.putIfAbsent(resource) == null) {
				publisher.publish(new ResourceLoaded(resource));
			} else {
				publisher.publish(new Warning("Resource identified by {} was created but already in the cache!"));
			}
		} catch (Exception e) {
			publisher.publish(new ResourceError(identifier, e));
		}

		// return whatever is in the cache
		return resourceCache.get(identifier);
	}

	private <T extends Resource<A>, A>  ResourceCreator<A, T> findCreator(ResourceIdentifier<T, A> identifier) {
		ResourceCreator<A, T> resourceCreator = resourceCache.getCreator(identifier.resourceClass);
		assert resourceCreator != null : "no ResourceCreator for " + identifier.resourceClass;
		return resourceCreator;
	}

	/**
	 * Converts a resource identifier with a list of locations into a list
	 * of resource identifiers with one location apiece
	 */
	private <T extends Resource<A>, A> List<ResourceIdentifier<T, A>> resourceIdentifiers(ResourceIdentifier<T, A> identifier) {
		List<ResourceIdentifier<T, A>> attempts;
		if (identifier.base.locations().size() > 1) {
			attempts = identifier.base.locations().stream().map(
				location -> resourceIdentifierMaker.make(identifier.resourceClass, location, identifier.name, identifier.argument)
			).collect(Collectors.toList());
		} else {
			attempts = Collections.singletonList(identifier);
		}
		return attempts;
	}

	private Closer acquire(ResourceIdentifier<?, ?> identifier) throws InterruptedException {
		ResourceTask owner = resourcesInProgress.putIfAbsent(identifier, currentTask.currentAs(ResourceTask.class));
		if (owner != null) {
			if (!owner.await(2, SECONDS)) {
				return () -> {};
			}
		}
		return () -> resourcesInProgress.remove(identifier, currentTask.currentAs(ResourceTask.class));
	}
}
