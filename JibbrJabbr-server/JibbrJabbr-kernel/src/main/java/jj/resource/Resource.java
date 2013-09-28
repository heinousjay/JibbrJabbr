package jj.resource;


/**
 * represents some resource loaded from the file system
 * @author jason
 *
 */
public interface Resource {
	
	/**
	 * The base name of this resource, which is the filename from the base
	 * path configured on the server, not including the extension in most cases
	 * @return
	 */
	String baseName();

	/**
	 * uri to this resource, expressed as a path relative to
	 * the absolute root that we serve, including the sha1 of
	 * the resource in the path so as to uniquely identify
	 * contents
	 * @return
	 */
	String uri();
	
	/**
	 * sha1 of the file bytes
	 * @return
	 */
	String sha1();

	/**
	 * Adds a dependent resource to this resource, which will propagate
	 * reloads to the dependent
	 * 
	 * @param dependent
	 */
	void addDependent(Resource dependent);
}