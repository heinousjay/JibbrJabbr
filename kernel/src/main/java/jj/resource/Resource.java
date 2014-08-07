package jj.resource;

import java.net.URI;
import java.nio.charset.Charset;



/**
 * <p>
 * represent a resource in the resource system
 * 
 * <p>
 * do not implement this directly, extend {@link AbstractResource} instead
 * 
 * @author jason
 *
 */
public interface Resource {
	
	/**
	 * The base location of this resource
	 */
	Location base();
	
	/**
	 * The name of this resource
	 */
	String name();

	/**
	 * uri to this resource
	 */
	URI uri();
	
	/**
	 * sha1 of the resource
	 */
	String sha1();
	
	/**
	 * charset of the resource, if it represents text, null otherwise
	 */
	Charset charset();

	/**
	 * Adds a dependent resource to this resource, which will propagate
	 * reloads to the dependent
	 */
	void addDependent(Resource dependent);
	
	/**
	 * The key in the cache where this resource is stored
	 */
	ResourceKey cacheKey();
	
	/**
	 * flag indicating this live status of this resource.  a resource is considered alive
	 * between the time of its creation and the time it is removed from the resource cache.
	 */
	boolean alive();
}