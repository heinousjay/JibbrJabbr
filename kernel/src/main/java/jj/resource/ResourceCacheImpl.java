package jj.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
	
	private final ConcurrentMap<ResourceKey, Resource<?>> resourceCache = new ConcurrentHashMap<>(128, 0.75F, 4);

	@Inject
	ResourceCacheImpl(final ResourceCreators resourceCreators) {
		this.resourceCreators = resourceCreators;
	}

	/**
	 * @param uri
	 * @return
	 */
	@Override
	public List<Resource<?>> findAllByUri(URI uri) {
		
		List<Resource<?>> result = new ArrayList<>();
		
		for (SimpleResourceCreator<?, ? extends Resource<?>> resourceCreator : resourceCreators) {
			Resource<?> it = get(new ResourceKey(resourceCreator.type(), uri));
			if (it != null) result.add(it);
		}
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Returns an unmodifiable snapshot of the current Resource instances.  This information is
	 * immediately out of date, cannot be manipulated, and is in no particular order.
	 * @return
	 */
	List<Resource<?>> allResources() {
		return Collections.unmodifiableList(new ArrayList<>(resourceCache.values()));
	}
	
	@Listener
	void on(ServerStopping event) {
		resourceCache.clear();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <A, T extends Resource<A>> ResourceCreator<A, T> getCreator(final Class<T> type) {
		return (ResourceCreator<A, T>) resourceCreators.get(type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <A, T extends Resource<A>> T get(ResourceKey key) {
		return (T)resourceCache.get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A, T extends Resource<A>> T putIfAbsent(ResourceKey key, T value) {
		return (T)resourceCache.putIfAbsent(key, value);
	}

	@Override
	public <A, T extends Resource<A>> boolean replace(ResourceKey key, T oldValue, T newValue) {
		return resourceCache.replace(key, oldValue, newValue);
	}

	@Override
	public <A, T extends Resource<A>> boolean remove(ResourceKey cacheKey, T resource) {
		return resourceCache.remove(cacheKey, resource);
	}
	
	int size() {
		return resourceCache.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName()).append(" {\n");
		for (Entry<ResourceKey, Resource<?>> entry : resourceCache.entrySet()) {
			sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
		}
 		return sb.append("}").toString();
	}
}
