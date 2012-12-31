package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.IOThread;

/**
 * coordinates access to the resource cache for the outside
 * world
 * @author jason
 *
 */
class ResourceFinderImpl implements ResourceFinder {
	
	private final Logger log = LoggerFactory.getLogger(ResourceFinderImpl.class);

	private final ResourceCache resourceCache;
	
	private final Map<Class<?>, ResourceCreator<?>> resourceCreators;
	
	private final ResourceWatchService resourceWatchService;
	
	ResourceFinderImpl(
		final ResourceCache resourceCache,
		final ResourceCreator<?>[] resourceCreators,
		final ResourceWatchService resourceWatchService
	) {
		this.resourceCache = resourceCache;
		this.resourceCreators = makeResourceCreatorsMap(resourceCreators);
		this.resourceWatchService = resourceWatchService;
	}
	
	// goddam, java generics get ugly sometimes
	private 
	Map<Class<?>, ResourceCreator<?>> 
	makeResourceCreatorsMap(final ResourceCreator<?>[] resourceCreators) {
		Map<Class<?>, ResourceCreator<?>> result = new HashMap<>();
		for (ResourceCreator<?> resourceCreator : resourceCreators) {
			result.put(resourceCreator.type(), resourceCreator);
		}
		return Collections.unmodifiableMap(result);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Resource> T findResource(T resource) {
		return resource == null ? null : (T)findResource(resource.getClass(), resource.baseName(), resource.creationArgs());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Resource> T findResource(
		final Class<T> resourceClass,
		String baseName,
		Object... args
	) {
		log.debug("checking in cache for {} at {}", resourceClass.getSimpleName(), baseName);
		T result = (T)resourceCache.get(resourceCreators.get(resourceClass).toPath(baseName, args).toUri());
		log.debug("result {}", result);
		return result;
	}

	@SuppressWarnings("unchecked")
	@IOThread
	@Override
	public  <T extends Resource> T loadResource(
		final Class<T> resourceClass,
		String baseName,
		Object... args
	) {
		T result = null;
		ResourceCreator<T> resourceCreator = 
			(ResourceCreator<T>)resourceCreators.get(resourceClass);
		if (resourceCreator == null) {
			throw new AssertionError("need a ResourceCreator for " + resourceClass);
		}
		Path path = resourceCreator.toPath(baseName, args);
		URI pathUri = path.toUri();
		try {
			result = (T)resourceCache.get(pathUri);
			if (result == null) {
				log.debug("loading {} at {}", resourceClass.getSimpleName(), path);
				Resource resource = resourceCreator.create(baseName, args);
				if (resourceCache.putIfAbsent(pathUri, resource) == null) {
					// if this was the first time we put this in the cache,
					// we set up a file watch on it for background reloads
					resourceWatchService.watch(resource);
				}
			} else if (result.needsReplacing()) {
				log.debug("replacing {} at {}", resourceClass.getSimpleName(), path);
				if (!resourceCache.replace(pathUri, result, resourceCreator.create(baseName, args))){
					log.warn("replacement failed, someone snuck in behind me? {} at {}", resourceClass.getSimpleName(), path);
				}
			}
			result = (T)resourceCache.get(pathUri);
		} catch (NullPointerException | NoSuchFileException e) {
			log.debug("couldn't find {} at {}", resourceClass.getSimpleName(), path);
			log.debug("", e);
		} catch (IOException ioe) {
			log.error("trouble loading {} at  {}", resourceClass.getSimpleName(), path);
			log.error("", ioe);
		}
		return result;
	}
}
