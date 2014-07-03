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
	 * @param resourceClass The type of <code>Resource</code>
	 * @param base The {@link Location} of the <code>Resource</code>
	 * @param name The name of the <code>Resource</code>
	 * @param args The creation arguments of the <code>Resource</code>
	 * @return the {@link Resource}, or null if not found
	 */
	<T extends Resource> T findResource(Class<T> resourceClass, Location base, String name, Object...args);
	
	/**
	 * <p>
	 * loads a resource matching the given resource spec, if necessary, and populates
	 * the cache.  can only be called via a {@link ResourceTask}.  if the resource spec does
	 * not identify a valid resource, this returns null. if a resource is returned from this method,
	 * then it will be watched for changes and automatically updated
	 * 
	 * @param resourceClass The type of <code>Resource</code>
	 * @param base The {@link Location} of the <code>Resource</code>
	 * @param name The name of the <code>Resource</code>
	 * @param args The creation arguments of the <code>Resource</code>
	 * @return the {@link Resource}, or null if not found
	 */
	@ResourceThread
	<T extends Resource> T loadResource(Class<T> resourceClass, Location base, String name, Object...args);
}
