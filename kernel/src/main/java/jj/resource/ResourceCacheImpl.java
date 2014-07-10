package jj.resource;

import io.netty.util.internal.PlatformDependent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource.
 * @author jason
 *
 */
@Singleton
@Subscriber
class ResourceCacheImpl implements ResourceCache {
	
	private final ResourceCreators resourceCreators;
	
	ConcurrentMap<ResourceKey, Resource> resourceCache =
		PlatformDependent.newConcurrentHashMap(128, 0.75F, 4);

	@Inject
	ResourceCacheImpl(final ResourceCreators resourceCreators) {
		this.resourceCreators = resourceCreators;
	}

	/**
	 * @param uri
	 * @return
	 */
	public List<Resource> findAllByUri(URI uri) {
		
		List<Resource> result = new ArrayList<>();
		
		for (SimpleResourceCreator<? extends Resource> resourceCreator : resourceCreators) {
			Resource it = get(new ResourceKey(resourceCreator.type(), uri));
			if (it != null) result.add(it);
		}
		return Collections.unmodifiableList(result);
	}
	
	@Listener
	public void serverStopping(ServerStopping event) {
		resourceCache.clear();
	}
	
	@Override
	public <T extends Resource> ResourceCreator<T> getCreator(Class<T> type) {
		return resourceCreators.get(type);
	}
	
	@Override
	public Resource get(ResourceKey key) {
		return resourceCache.get(key);
	}

	@Override
	public Resource putIfAbsent(ResourceKey key, Resource value) {
		return resourceCache.putIfAbsent(key, value);
	}

	@Override
	public boolean replace(ResourceKey key, Resource oldValue, Resource newValue) {
		return resourceCache.replace(key, oldValue, newValue);
	}

	@Override
	public boolean remove(ResourceKey cacheKey, Resource resource) {
		return resourceCache.remove(cacheKey, resource);
	}
	
	int size() {
		return resourceCache.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName()).append(" {\n");
		for (Entry<ResourceKey, Resource> entry : resourceCache.entrySet()) {
			sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
		}
 		return sb.append("}").toString();
	}
}
