package jj.resource;

import io.netty.util.internal.PlatformDependent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerShutdownListener;
import jj.JJServerStartupListener;
import jj.configuration.Configuration;
import jj.execution.ExecutionConfiguration;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource.
 * @author jason
 *
 */
@Singleton
class ResourceCacheImpl implements JJServerStartupListener, JJServerShutdownListener, ResourceCache {
	
	private final ResourceCreators resourceCreators;
	
	private final Configuration configuration;
	
	private AtomicReference<ConcurrentMap<ResourceCacheKey, Resource>> delegate;

	@Inject
	ResourceCacheImpl(final ResourceCreators resourceCreators, final Configuration configuration) {
		this.resourceCreators = resourceCreators;
		this.configuration = configuration;
		
		// we're going to have our config resource, at least, before we get started
		delegate = new AtomicReference<>(PlatformDependent.<ResourceCacheKey, Resource>newConcurrentHashMap(4, 0.75F, 3));
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
	public void start() throws Exception {
		ConcurrentMap<ResourceCacheKey, Resource> old = 
			delegate.getAndSet(
				PlatformDependent.<ResourceCacheKey, Resource>newConcurrentHashMap(
					128,
					0.75F,
					configuration.get(ExecutionConfiguration.class).ioThreads()
				)
			); 
		delegate.get().putAll(old);
	}
	
	@Override
	public Priority startPriority() {
		// it's pretty important we're ready to go early on.
		return Priority.NearHighest;
	}

	@Override
	public void stop() {
		// make sure we start fresh if we get restarted
		delegate.get().clear();
	}
	
	@Override
	public Resource get(ResourceCacheKey key) {
		return delegate.get().get(key);
	}

	@Override
	public Resource putIfAbsent(ResourceCacheKey key, Resource value) {
		return delegate.get().putIfAbsent(key, value);
	}

	@Override
	public boolean replace(ResourceCacheKey key, Resource oldValue, Resource newValue) {
		return delegate.get().replace(key, oldValue, newValue);
	}

	@Override
	public boolean remove(ResourceCacheKey cacheKey, Resource resource) {
		return delegate.get().remove(cacheKey, resource);
	}
	
	int size() {
		return delegate.get().size();
	}
}
