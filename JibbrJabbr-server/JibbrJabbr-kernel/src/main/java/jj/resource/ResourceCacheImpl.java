package jj.resource;

import io.netty.util.internal.PlatformDependent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerShutdownListener;
import jj.execution.IOExecutor;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource.
 * @author jason
 *
 */
@Singleton
class ResourceCacheImpl implements JJServerShutdownListener, ResourceCache {
	
	private final ResourceCreators resourceCreators;
	
	private final ConcurrentMap<ResourceCacheKey, Resource> delegate;

	@Inject
	ResourceCacheImpl(final ResourceCreators resourceCreators) {
		delegate = PlatformDependent.newConcurrentHashMap(16, 0.75F, IOExecutor.WORKER_COUNT);
		this.resourceCreators = resourceCreators;
	}

	/**
	 * @param uri
	 * @return
	 */
	@Override
	public List<Resource> findAllByUri(URI uri) {
		
		List<Resource> result = new ArrayList<>();
		
		for (ResourceCreator<? extends Resource> resourceCreator : resourceCreators) {
			Resource it = get(resourceCreator.cacheKey(uri));
			if (it != null) result.add(it);
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public void stop() {
		// make sure we start fresh if we get restarted
		delegate.clear();
	}
	
	@Override
	public Resource get(ResourceCacheKey key) {
		return delegate.get(key);
	}

	@Override
	public Resource putIfAbsent(ResourceCacheKey key, Resource value) {
		return delegate.putIfAbsent(key, value);
	}

	@Override
	public boolean replace(ResourceCacheKey key, Resource oldValue, Resource newValue) {
		return delegate.replace(key, oldValue, newValue);
	}

	@Override
	public boolean remove(ResourceCacheKey cacheKey, Resource resource) {
		return delegate.remove(cacheKey, resource);
	}
	
	int size() {
		return delegate.size();
	}
}
