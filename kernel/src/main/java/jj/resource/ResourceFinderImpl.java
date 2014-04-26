package jj.resource;

import io.netty.util.internal.PlatformDependent;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.configuration.Location;
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
	
	private final ConcurrentMap<ResourceKey, ResourceTask> resourcesInProgress = PlatformDependent.newConcurrentHashMap();

	private final ResourceCache resourceCache;
	
	private final ResourceWatchService resourceWatchService;
	
	private final Publisher publisher;
	
	private final IsThread isThread;
	
	private final CurrentTask currentTask;
	
	@Inject
	ResourceFinderImpl(
		final ResourceCache resourceCache,
		final ResourceWatchService resourceWatchService,
		final Publisher publisher,
		final IsThread isThread,
		final CurrentTask currentTask
	) {
		this.resourceCache = resourceCache;
		this.resourceWatchService = resourceWatchService;
		this.publisher = publisher;
		this.isThread = isThread;
		this.currentTask = currentTask;
	}
	
	@Override
	public <T extends Resource> T findResource(
		final Class<T> resourceClass,
		Location locations,
		String name,
		Object... args
	) {
		T result = null;
		
		for (Location base : locations.locations()) {
			result = result == null ?
				resourceClass.cast(resourceCache.get(resourceCache.getCreator(resourceClass).resourceKey(base, name, args))) :
				result;
		}
		
		return result;
	}
	
	@Override
	@ResourceThread
	public <T extends Resource> T loadResource(
		final Class<T> resourceClass,
		Location bundle,
		String name,
		Object...arguments
	) {
		assert isThread.forResourceTask() : "Can only call loadResource from an I/O thread";
		
		ResourceCreator<T> resourceCreator = resourceCache.getCreator(resourceClass);
		
		assert resourceCreator != null : "no ResourceCreator for " + resourceClass;
		T result = null;
		
		for (Location base : bundle.locations()) {
		
			ResourceKey cacheKey = resourceCreator.resourceKey(base, name, arguments);
			acquire(cacheKey);
			try {
				result = resourceClass.cast(resourceCache.get(cacheKey));
				if (result == null) {
					createResource(base, name, resourceCreator, cacheKey, arguments);
				} else if (((AbstractResource)result).isObselete()) {
					replaceResource(base, name, resourceCreator, result, cacheKey, arguments);
				}
				result = resourceClass.cast(resourceCache.get(cacheKey));
			
			} catch (Exception e) {
				publisher.publish(new ResourceError(resourceClass, base, name, arguments, e));
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
			owner.await();
		}
	}
	
	private void release(ResourceKey slot) {
		resourcesInProgress.remove(slot, currentTask.currentAs(ResourceTask.class));
	}

	private <T extends Resource> void replaceResource(
		final Location base,
		String name,
		ResourceCreator<T> resourceCreator,
		T result,
		ResourceKey cacheKey,
		Object...arguments
	) throws IOException {
		
		T resource = resourceCreator.create(base, name, arguments);
		
		if (resource == null) {
			publisher.publish(new ResourceNotFound(resourceCreator.type(), base, name, arguments));
		} else {
			if (resourceCache.replace(cacheKey, result, resource)) {
				publisher.publish(new ResourceReloaded(resource.getClass(), base, name, arguments));
			} // else we wasted our time, something else replaced it already
		} 
	}

	private <T extends Resource> void createResource(
		Location base,
		String name,
		ResourceCreator<T> resourceCreator,
		ResourceKey cacheKey,
		Object...arguments
	) throws IOException {
		
		T resource = resourceCreator.create(base, name, arguments);
		if (resource == null) {
			publisher.publish(new ResourceNotFound(resourceCreator.type(), base, name, arguments));
		} else {
			publisher.publish(new ResourceLoaded(resourceCreator.type(), base, name, arguments));
			if (
				resourceCache.putIfAbsent(cacheKey, resource) == null &&
				resource instanceof FileResource
			) {
				// if this was the first time we put this in the cache,
				// we set up a file watch on it for background reloads
				resourceWatchService.watch((FileResource)resource);
			}
		}
	}
}
