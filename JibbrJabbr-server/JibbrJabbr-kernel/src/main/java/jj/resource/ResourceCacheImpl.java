package jj.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerListener;
import jj.execution.IOExecutor;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource
 * @author jason
 *
 */
@Singleton
class ResourceCacheImpl extends ConcurrentHashMap<ResourceCacheKey, Resource> implements JJServerListener, ResourceCache {

	private static final long serialVersionUID = 1L;
	
	private final ResourceCreators resourceCreators;

	@Inject
	ResourceCacheImpl(final ResourceCreators resourceCreators) {
		super(16, 0.75F, IOExecutor.WORKER_COUNT);
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

	/**
	 * @param uri
	 */
	@Override
	public void removeAllByUri(URI uri) {
		for (ResourceCreator<? extends Resource> resourceCreator : resourceCreators) {
			remove(resourceCreator.cacheKey(uri));
		}
	}

	@Override
	public void start() throws Exception {
		// make sure we start fresh if we get restarted
		clear();
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
