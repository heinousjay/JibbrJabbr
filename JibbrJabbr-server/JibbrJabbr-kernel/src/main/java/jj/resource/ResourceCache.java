package jj.resource;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.IOExecutor;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource
 * @author jason
 *
 */
@Singleton
class ResourceCache extends ConcurrentHashMap<ResourceCacheKey, Resource> {

	private static final long serialVersionUID = 1L;

	@Inject
	ResourceCache() {
		super(16, 0.75F, IOExecutor.WORKER_COUNT);
	}
}
