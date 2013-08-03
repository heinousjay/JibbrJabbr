package jj.resource;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;

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
	 * The path of the resource
	 * @return
	 */
	Path path();
	
	/**
	 * the arguments used to create this resource
	 * @return
	 */
	Object[] creationArgs();
	
	/**
	 * time this resource was last modified
	 * @return
	 */
	FileTime lastModified();
	
	/**
	 * size of the file in bytes
	 * @return
	 */
	long size();
	
	/**
	 * time this resource was last modified as
	 * a java.util.Date
	 * @return
	 */
	Date lastModifiedDate();

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
	 * the mime of the resource, including charset if it
	 * is textual.  (always utf-8)
	 * @return
	 */
	String mime();
}