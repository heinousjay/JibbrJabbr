package jj.resource;

import jj.IOThread;

/**
 * Inject this to get resources from the filesystem
 * @author jason
 *
 */
public interface ResourceFinder {
	
	/**
	 * looks in the resource cache for the given resource, which may be a
	 * newer version of the same resource or the same resource. if this returns null,
	 * that means the resource was deleted
	 * @param resource
	 * @return
	 */
	<T extends Resource> T findResource(T resource);
	
	/**
	 * looks in the resource cache for a resource matching the given spec,
	 * which is a resource dependent set of lookup parameters.  returns null
	 * if no such resource is in the cache, which doesn't mean it's not real!
	 * it just means we didn't load it yet
	 * @param resourceClass
	 * @param baseName
	 * @param args
	 * @return
	 */
	<T extends Resource> T findResource(Class<T> resourceClass, String baseName, Object...args);
	
	@IOThread
	Resource loadResource(String baseName, Object...args);
	
	/**
	 * loads a resource matching the given resource spec, if necessary, and populates
	 * the cache.  can only be called from an IO thread.  if the resource spec does
	 * not identify a valid resource, this returns null. if a resource is returned from this method,
	 * then it will be watched for changes and automatically updated
	 * @param resourceClass
	 * @param baseName
	 * @param args
	 * @return
	 */
	@IOThread
	<T extends Resource> T loadResource(Class<T> resourceClass, String baseName, Object...args);
}
