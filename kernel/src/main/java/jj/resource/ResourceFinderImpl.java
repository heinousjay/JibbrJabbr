package jj.resource;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.event.Publisher;
import jj.execution.IOThread;
import jj.execution.IsThread;

/**
 * coordinates access to the resource cache for the outside
 * world.
 * @author jason
 *
 */
@Singleton
class ResourceFinderImpl implements ResourceFinder {
	
	private final Logger log = LoggerFactory.getLogger(ResourceFinderImpl.class);

	private final ResourceCache resourceCache;
	
	private final ResourceWatchService resourceWatchService;
	
	private final Publisher publisher;
	
	private final IsThread isThread;
	
	@Inject
	ResourceFinderImpl(
		final ResourceCache resourceCache,
		final ResourceWatchService resourceWatchService,
		final Publisher publisher,
		final IsThread isThread
	) {
		this.resourceCache = resourceCache;
		this.resourceWatchService = resourceWatchService;
		this.publisher = publisher;
		this.isThread = isThread;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Resource> T findResource(T resource) {
		return resource == null ? 
			null : 
			(T)findResource(
				resource.getClass(),
				resource.baseName(),
				((AbstractResource)resource).creationArgs()
			);
	}
	
	@Override
	public <T extends Resource> T findResource(
		final Class<T> resourceClass,
		String baseName,
		Object... args
	) {
		log.trace("checking in resource cache for {} at {}", resourceClass.getSimpleName(), baseName);
		T result = resourceClass.cast(resourceCache.get(resourceCache.getCreator(resourceClass).cacheKey(baseName, args)));
		log.trace("result {}", result);
		return result;
	}
	
	@IOThread
	@Override
	public  <T extends Resource> T loadResource(
		final Class<T> resourceClass,
		String baseName,
		Object... args
	) {
		assert isThread.forIO() : "Can only call loadResource from an I/O thread";
		
		ResourceCreator<T> resourceCreator = resourceCache.getCreator(resourceClass);
		
		assert resourceCreator != null : "no ResourceCreator for " + resourceClass;
		T result = null;
		
		ResourceCacheKey cacheKey = resourceCreator.cacheKey(baseName, args);
		try {
			result = resourceClass.cast(resourceCache.get(cacheKey));
			if (result == null) {
				createResource(baseName, resourceCreator, cacheKey, args);
			} else if (((AbstractResource)result).isObselete()) {
				replaceResource(baseName, resourceCreator, result, cacheKey, args);
			}
			result = resourceClass.cast(resourceCache.get(cacheKey));
		
		} catch (Exception e) {
			log.error("trouble loading {} at  {}", resourceClass.getSimpleName(), cacheKey);
			log.error("", e);
		}
		
		return result;
	}

	private <T extends Resource> void replaceResource(
		String baseName,
		ResourceCreator<T> resourceCreator,
		T result,
		ResourceCacheKey cacheKey,
		Object... args
	) throws IOException {
		
		log.trace("replacing {} at {}", resourceCreator.type().getSimpleName(), cacheKey);
		
		T resource = resourceCreator.create(baseName, args);
		// should a 
		if (resource != null && !resourceCache.replace(cacheKey, result, resource)){
			log.warn("{} at {} replacement failed, someone snuck in behind me?", resourceCreator.type().getSimpleName(), cacheKey);
		}
	}

	private <T extends Resource> void createResource(
		String name,
		ResourceCreator<T> resourceCreator,
		ResourceCacheKey cacheKey,
		Object...arguments
	) throws IOException {
		
		log.trace("loading {} at {}", resourceCreator.type().getSimpleName(), cacheKey);
		
		T resource = resourceCreator.create(name, arguments);
		if (resource == null) {
			publisher.publish(new ResourceNotFoundEvent(resourceCreator.type(), name, arguments));
		} else {
			publisher.publish(new ResourceLoadedEvent(resourceCreator.type(), name, arguments));
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
