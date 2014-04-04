package jj.resource;

import jj.configuration.Location;

/**
 * Inject this to get resources from the filesystem
 * @author jason
 *
 */
public interface ResourceFinder {
	
	/**
	 * <p>
	 * looks in the resource cache for a resource matching the given spec,
	 * which is a resource dependent set of lookup parameters.  returns null
	 * if no such resource is in the cache, which means it hasn't been loaded
	 * yet
	 * 
	 * @param resourceClass
	 * @param baseName
	 * @param args
	 * @return
	 */
	<T extends Resource> T findResource(Class<T> resourceClass, Location base, String name, Object...args);
	
	/**
	 * <p>
	 * loads a resource matching the given resource spec, if necessary, and populates
	 * the cache.  can only be called from an IO thread.  if the resource spec does
	 * not identify a valid resource, this returns null. if a resource is returned from this method,
	 * then it will be watched for changes and automatically updated
	 * 
	 * @param resourceClass
	 * @param baseName
	 * @param args
	 * @return
	 */
	@ResourceThread
	<T extends Resource> T loadResource(Class<T> resourceClass, Location base, String name, Object...args);
}
