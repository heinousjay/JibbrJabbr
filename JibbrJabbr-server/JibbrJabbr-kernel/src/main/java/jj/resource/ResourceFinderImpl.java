package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.IOThread;
import jj.JJExecutors;

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
	
	private final Map<Class<?>, ResourceCreator<?>> resourceCreators;
	
	private final ResourceWatchService resourceWatchService;
	
	private final JJExecutors executors;
	
	@Inject
	ResourceFinderImpl(
		final ResourceCache resourceCache,
		final Set<ResourceCreator<?>> resourceCreators,
		final ResourceWatchService resourceWatchService,
		final JJExecutors executors
	) {
		this.resourceCache = resourceCache;
		this.resourceCreators = makeResourceCreatorsMap(resourceCreators);
		this.resourceWatchService = resourceWatchService;
		this.executors = executors;
	}
	
	// goddam, java generics get ugly sometimes
	private 
	Map<Class<?>, ResourceCreator<?>> 
	makeResourceCreatorsMap(final Set<ResourceCreator<?>> resourceCreators) {
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
		log.trace("checking in resource cache for {} at {}", resourceClass.getSimpleName(), baseName);
		T result = (T)resourceCache.get(resourceCreators.get(resourceClass).toPath(baseName, args).toUri());
		log.trace("result {}", result);
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
		
		assert executors.isIOThread() : "Can only call loadResource from an I/O thread";
		
		ResourceCreator<T> resourceCreator = (ResourceCreator<T>)resourceCreators.get(resourceClass);
		assert resourceCreator != null : "no ResourceCreator for " + resourceClass;
		
		T result = null;
		
		Path path = resourceCreator.toPath(baseName, args);
		URI pathUri = path.toUri();
		try {
			result = (T)resourceCache.get(pathUri);
			if (result == null) {
				log.trace("loading {} at {}", resourceClass.getSimpleName(), path);
				Resource resource = resourceCreator.create(baseName, args);
				if (resourceCache.putIfAbsent(pathUri, resource) == null) {
					// if this was the first time we put this in the cache,
					// we set up a file watch on it for background reloads
					resourceWatchService.watch(resource);
				}
			} else if (result.needsReplacing()) {
				log.trace("replacing {} at {}", resourceClass.getSimpleName(), path);
				if (!resourceCache.replace(pathUri, result, resourceCreator.create(baseName, args))){
					log.warn("replacement failed, someone snuck in behind me? {} at {}", resourceClass.getSimpleName(), path);
				}
			}
			result = (T)resourceCache.get(pathUri);
		} catch (NullPointerException | NoSuchFileException e) {
			log.trace("couldn't find {} at {}", resourceClass.getSimpleName(), path);
		} catch (IOException ioe) {
			log.error("trouble loading {} at  {}", resourceClass.getSimpleName(), path);
			log.error("", ioe);
		}
		return result;
	}
}
