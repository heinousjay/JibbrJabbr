package jj.resource;



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
	 * @return
	 */
	String name();

	/**
	 * uri to this resource, expressed as a relative path
	 * @return
	 */
	String uri();
	
	/**
	 * sha1 of the resource
	 * @return
	 */
	String sha1();

	/**
	 * Adds a dependent resource to this resource, which will propagate
	 * reloads to the dependent
	 * @param dependent
	 */
	void addDependent(Resource dependent);
	
	ResourceKey cacheKey();
}