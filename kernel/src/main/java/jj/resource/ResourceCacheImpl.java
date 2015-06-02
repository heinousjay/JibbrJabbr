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
	
	private final ConcurrentMap<ResourceKey, AbstractResource> resourceCache = new ConcurrentHashMap<>(128, 0.75F, 4);

	@Inject
	ResourceCacheImpl(final ResourceCreators resourceCreators) {
		this.resourceCreators = resourceCreators;
	}

	/**
	 * @param uri
	 * @return
	 */
	public List<AbstractResource> findAllByUri(URI uri) {
		
		List<AbstractResource> result = new ArrayList<>();
		
		for (SimpleResourceCreator<? extends Resource> resourceCreator : resourceCreators) {
			AbstractResource it = get(new ResourceKey(resourceCreator.type(), uri));
			if (it != null) result.add(it);
		}
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Returns an unmodifiable snapshot of the current Resource instances.  This information is
	 * immediately out of date, cannot be manipulated, and is in no particular order.
	 * @return
	 */
	List<AbstractResource> allResources() {
		return Collections.unmodifiableList(new ArrayList<>(resourceCache.values()));
	}
	
	@Listener
	void on(ServerStopping event) {
		resourceCache.clear();
	}
	
	@Override
	public <T extends Resource> ResourceCreator<T> getCreator(Class<T> type) {
		return resourceCreators.get(type);
	}
	
	@Override
	public AbstractResource get(ResourceKey key) {
		return resourceCache.get(key);
	}

	@Override
	public AbstractResource putIfAbsent(ResourceKey key, AbstractResource value) {
		return resourceCache.putIfAbsent(key, value);
	}

	@Override
	public boolean replace(ResourceKey key, AbstractResource oldValue, AbstractResource newValue) {
		return resourceCache.replace(key, oldValue, newValue);
	}

	@Override
	public boolean remove(ResourceKey cacheKey, AbstractResource resource) {
		return resourceCache.remove(cacheKey, resource);
	}
	
	int size() {
		return resourceCache.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName()).append(" {\n");
		for (Entry<ResourceKey, AbstractResource> entry : resourceCache.entrySet()) {
			sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
		}
 		return sb.append("}").toString();
	}
}
