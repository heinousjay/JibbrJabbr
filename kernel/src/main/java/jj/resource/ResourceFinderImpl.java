package jj.resource;

import io.netty.util.internal.PlatformDependent;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.configuration.AppLocation;
import jj.configuration.AppLocation.AppLocationBundle;
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
	
	// TODO pick a central logger for this stuff
	private final Logger log = LoggerFactory.getLogger(ResourceFinderImpl.class);
	
	private final ConcurrentMap<ResourceCacheKey, ResourceTask> resourcesInProgress = PlatformDependent.newConcurrentHashMap();

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
		AppLocationBundle bundle,
		String name,
		Object... args
	) {
		T result = null;
		
		for (AppLocation base : bundle.locations()) {
			result = result == null ?
				findResource(resourceClass, base, name, args) :
				result;
		}
		
		return result;
	}
	
	@Override
	public <T extends Resource> T findResource(
		final Class<T> resourceClass,
		AppLocation base,
		String name,
		Object... args
	) {
		return resourceClass.cast(resourceCache.get(resourceCache.getCreator(resourceClass).cacheKey(base, name, args)));
	}
	
	@Override
	public <T extends Resource> T loadResource(
		final Class<T> resourceClass,
		AppLocationBundle bundle,
		String name,
		Object... args
	) {
		T result = null;
		
		for (AppLocation base : bundle.locations()) {
			result = result == null ?
				loadResource(resourceClass, base, name, args) :
				result;
		}
		
		return result;
	}
	
	@ResourceThread
	@Override
	public  <T extends Resource> T loadResource(
		final Class<T> resourceClass,
		AppLocation base,
		String name,
		Object...arguments
	) {
		assert isThread.forResourceTask() : "Can only call loadResource from an I/O thread";
		
		ResourceCreator<T> resourceCreator = resourceCache.getCreator(resourceClass);
		
		assert resourceCreator != null : "no ResourceCreator for " + resourceClass;
		T result = null;
		
		ResourceCacheKey cacheKey = resourceCreator.cacheKey(base, name, arguments);
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
			log.error("trouble loading {} at  {}", resourceClass.getSimpleName(), cacheKey);
			log.error("", e);
		} finally {
			release(cacheKey);
		}
		
		return result;
	}
	
	private void acquire(ResourceCacheKey slot) {
		ResourceTask owner = resourcesInProgress.putIfAbsent(slot, currentTask.currentAs(ResourceTask.class));
		if (owner != null) {
			owner.await();
		}
	}
	
	private void release(ResourceCacheKey slot) {
		resourcesInProgress.remove(slot, currentTask.currentAs(ResourceTask.class));
	}

	private <T extends Resource> void replaceResource(
		final AppLocation base,
		String name,
		ResourceCreator<T> resourceCreator,
		T result,
		ResourceCacheKey cacheKey,
		Object...arguments
	) throws IOException {
		
		T resource = resourceCreator.create(base, name, arguments);
		
		if (resource == null) {
			publisher.publish(new ResourceNotFoundEvent(resourceCreator.type(), base, name, arguments));
		} else {
			if (resourceCache.replace(cacheKey, result, resource)) {
				publisher.publish(new ResourceReloadedEvent(resource.getClass(), base, name, arguments));
			} else {
				System.err.println("resource replacement failed for " + resource);
				log.warn("{} at {} replacement failed, someone snuck in behind me?", resourceCreator.type().getSimpleName(), cacheKey);
			}
		} 
	}

	private <T extends Resource> void createResource(
		AppLocation base,
		String name,
		ResourceCreator<T> resourceCreator,
		ResourceCacheKey cacheKey,
		Object...arguments
	) throws IOException {
		
		T resource = resourceCreator.create(base, name, arguments);
		if (resource == null) {
			publisher.publish(new ResourceNotFoundEvent(resourceCreator.type(), base, name, arguments));
		} else {
			publisher.publish(new ResourceLoadedEvent(resourceCreator.type(), base, name, arguments));
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
