package jj.resource;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.execution.CurrentTask;

/**
 * coordinates access to the resource cache for the outside
 * world.
 * @author jason
 *
 */
@Singleton
class ResourceFinderImpl implements ResourceFinder {
	
	private final ConcurrentMap<ResourceKey, ResourceTask> resourcesInProgress = new ConcurrentHashMap<>();

	private final ResourceCache resourceCache;
	
	private final ResourceWatchService resourceWatchService;
	
	private final Publisher publisher;
	
	private final CurrentTask currentTask;
	
	@Inject
	ResourceFinderImpl(
		final ResourceCache resourceCache,
		final ResourceWatchService resourceWatchService,
		final Publisher publisher,
		final CurrentTask currentTask
	) {
		this.resourceCache = resourceCache;
		this.resourceWatchService = resourceWatchService;
		this.publisher = publisher;
		this.currentTask = currentTask;
	}
	
	@Override
	public <T extends Resource<Void>> T findResource(
		final Class<T> resourceClass,
		Location locations,
		String name) {
		return findResource(resourceClass, locations, name, null);
	}
	
	@Override
	public <A, T extends Resource<A>> T findResource(
		Class<T> resourceClass,
		Location locations,
		String name,
		A argument
	) {
		T result = null;
		
		for (Location base : locations.locations()) {
			result = result == null ?
				cacheLoadAttempt(resourceClass, name, argument, base) :
				result;
		}
		
		return result;
	}

	private <A, T extends Resource<A>> T cacheLoadAttempt(Class<T> resourceClass, String name, A argument, Location base) {
		return resourceClass.cast(resourceCache.get(resourceCache.getCreator(resourceClass).resourceKey(base, name, argument)));
	}
	
	@Override
	@ResourceThread
	public <T extends Resource<Void>> T loadResource(
		final Class<T> resourceClass,
		Location locations,
		String name) {
		return loadResource(resourceClass, locations, name, null);
	}
	
	@Override
	@ResourceThread
	public <A, T extends Resource<A>> T loadResource(
		final Class<T> resourceClass,
		Location bundle,
		String name,
		A argument
	) {
		assert currentTask.currentIs(ResourceTask.class) : "Can only call loadResource from a ResourceTask";
		
		ResourceCreator<A, T> resourceCreator = resourceCache.getCreator(resourceClass);
		
		assert resourceCreator != null : "no ResourceCreator for " + resourceClass;
		T result = null;
		
		for (Location base : bundle.locations()) {
		
			ResourceKey cacheKey = resourceCreator.resourceKey(base, name, argument);
			acquire(cacheKey);
			try {
				result = resourceClass.cast(resourceCache.get(cacheKey));
				if (result == null) {
					createResource(base, name, resourceCreator, cacheKey, argument);
				} else if (((AbstractResource<A>)result).isObselete()) {
					replaceResource(base, name, resourceCreator, result, cacheKey, argument);
				}
				result = resourceClass.cast(resourceCache.get(cacheKey));
			
			} catch (Exception e) {
				publisher.publish(new ResourceError(resourceClass, base, name, argument, e));
			} finally {
				release(cacheKey);
			}
			
			if (result != null) {
				break;
			}
		}
		
		return result;
	}
	
	private void acquire(ResourceKey slot) {
		ResourceTask owner = resourcesInProgress.putIfAbsent(slot, currentTask.currentAs(ResourceTask.class));
		if (owner != null) {
			owner.await(2, SECONDS);
		}
	}
	
	private void release(ResourceKey slot) {
		resourcesInProgress.remove(slot, currentTask.currentAs(ResourceTask.class));
	}

	private <A, T extends Resource<A>> void replaceResource(
		final Location base,
		String name,
		ResourceCreator<A, T> resourceCreator,
		T result,
		ResourceKey cacheKey,
		A argument
	) throws IOException {
		
		T resource = resourceCreator.create(base, name, argument);
		
		if (resource == null) {
			publisher.publish(new ResourceNotFound(resourceCreator.type(), base, name, argument));
		} else {
			if (resourceCache.replace(cacheKey, result, resource)) {
				publisher.publish(new ResourceReloaded((AbstractResource<A>)result));
			} // else we wasted our time, something else replaced it already
		} 
	}

	private <A, T extends Resource<A>> void createResource(
		Location base,
		String name,
		ResourceCreator<A, T> resourceCreator,
		ResourceKey cacheKey,
		A argument
	) throws IOException {
		
		T resource = resourceCreator.create(base, name, argument);
		if (resource == null) {
			publisher.publish(new ResourceNotFound(resourceCreator.type(), base, name, argument));
		} else {
			if (resourceCache.putIfAbsent(cacheKey, resource) == null) {
				// let the world know
				publisher.publish(new ResourceLoaded((AbstractResource<A>)resource));
				
				if (resource instanceof FileSystemResource) {
					// if this was the first time we put this in the cache,
					// we set up a file watch on it for background reloads
					resourceWatchService.watch((FileSystemResource)resource);
				}
			}
		}
	}
}
