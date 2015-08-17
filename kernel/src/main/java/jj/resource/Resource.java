package jj.resource;

import java.net.URI;
import java.nio.charset.Charset;



/**
 * <p>
 * represents a resource in the resource system
 * 
 * <p>
 * do not implement this directly, extend {@link AbstractResource} instead
 * 
 * @param <T> the type of creation argument. Void if none
 * 
 * @author jason
 *
 */
public interface Resource<T> {
	
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
	 * add a resource as a dependent to this resource. if this resource is killed, that will
	 * be propagated to all dependents in turn. there is no such idea as removing dependents,
	 * outside of resource death, so semantically it is important to arrange that a permanent
	 * relationship makes sense
	 */
	void addDependent(Resource<?> dependent);
	
	/**
	 * The key in the cache where this resource is stored
	 */
	ResourceKey cacheKey();
	
	/**
	 * flag indicating this live status of this resource.  a resource is considered alive
	 * between the time of its creation and the time it is removed from the resource cache.
	 */
	boolean alive();
	
	/**
	 * the argument, if any, used to parameterize creation
	 */
	T creationArg();
}