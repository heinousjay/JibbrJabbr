package jj.resource;

import jj.configuration.AppLocation;
import jj.configuration.AppLocation.AppLocationBundle;

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
	<T extends Resource> T findResource(Class<T> resourceClass, AppLocation base, String name, Object...args);
	
	/**
	 * <p>
	 * behaves the same as {@link #findResource(Class, AppLocation, String, Object...)} but searches
	 * each location in the AppLocationBundle in order, returning the first one found
	 * 
	 * 
	 * @param resourceClass
	 * @param bundle
	 * @param name
	 * @param args
	 * @return
	 */
	<T extends Resource> T findResource(Class<T> resourceClass, AppLocationBundle bundle, String name, Object...args);
	
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
	@IOThread
	<T extends Resource> T loadResource(Class<T> resourceClass, AppLocation base, String name, Object...args);
	
	/**
	 * <p>
	 * behaves the same as {@link #loadResource(Class, AppLocation, String, Object...)} but searches
	 * each location in the AppLocationBundle in order, returning the first one found
	 * 
	 * @param resourceClass
	 * @param bundle
	 * @param args
	 * @return
	 */
	@IOThread
	<T extends Resource> T loadResource(Class<T> resourceClass, AppLocationBundle bundle, String name, Object...args);
}
