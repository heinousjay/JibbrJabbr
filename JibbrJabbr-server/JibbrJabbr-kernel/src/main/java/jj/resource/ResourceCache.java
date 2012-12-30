package jj.resource;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import jj.IOExecutor;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource
 * @author jason
 *
 */
class ResourceCache extends ConcurrentHashMap<URI, Resource> {

	private static final long serialVersionUID = 1L;

	ResourceCache() {
		super(16, 0.75F, IOExecutor.WORKER_COUNT);
	}
}
