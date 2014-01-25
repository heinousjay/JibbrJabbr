package jj.resource;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.execution.IOThread;
import jj.execution.JJExecutor;

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
	
	private final ResourceCreators resourceCreators;
	
	private final ResourceWatchService resourceWatchService;
	
	private final JJExecutor executors;
	
	@Inject
	ResourceFinderImpl(
		final ResourceCache resourceCache,
		final ResourceCreators resourceCreators,
		final ResourceWatchService resourceWatchService,
		final JJExecutor executors
	) {
		this.resourceCache = resourceCache;
		this.resourceCreators = resourceCreators;
		this.resourceWatchService = resourceWatchService;
		this.executors = executors;
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
		T result = resourceClass.cast(resourceCache.get(resourceCreators.get(resourceClass).cacheKey(baseName, args)));
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
		assert executors.isIOThread() : "Can only call loadResource from an I/O thread";
		
		ResourceCreator<T> resourceCreator = resourceCreators.get(resourceClass);
		
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
		if (resource != null && !resourceCache.replace(cacheKey, result, resource)){
			log.warn("{} at {} replacement failed, someone snuck in behind me?", resourceCreator.type().getSimpleName(), cacheKey);
		}
	}

	private <T extends Resource> void createResource(
		String baseName,
		ResourceCreator<T> resourceCreator,
		ResourceCacheKey cacheKey,
		Object... args
	) throws IOException {
		
		log.trace("loading {} at {}", resourceCreator.type().getSimpleName(), cacheKey);
		
		T resource = resourceCreator.create(baseName, args);
		if (
			resource != null &&
			resourceCache.putIfAbsent(cacheKey, resource) == null &&
			resource instanceof FileResource
		) {
			// if this was the first time we put this in the cache,
			// we set up a file watch on it for background reloads
			resourceWatchService.watch((FileResource)resource);
			
		}
	}
}