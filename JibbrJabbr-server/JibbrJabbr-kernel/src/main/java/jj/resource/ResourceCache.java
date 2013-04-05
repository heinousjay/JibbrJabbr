package jj.resource;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.IOExecutor;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource
 * @author jason
 *
 */
@Singleton
class ResourceCache extends ConcurrentHashMap<URI, Resource> {

	private static final long serialVersionUID = 1L;

	@Inject
	ResourceCache() {
		super(16, 0.75F, IOExecutor.WORKER_COUNT);
	}
}
