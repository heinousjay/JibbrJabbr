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
	 * Register a dependency on another resource.  This means that
	 * when an update to a dependency is detected, the dependent will
	 * be reloaded, even if it has no changes of its own
	 * 
	 * @param dependency
	 */
	void dependsOn(Resource dependency);
}