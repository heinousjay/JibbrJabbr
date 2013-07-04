package jj.resource;

import java.io.IOException;
import java.nio.file.Path;

/**
 * represents the ability to create a resource given a baseName and
 * some unspecified arguments
 * @author jason
 *
 * @param <T> The type of resource we create
 */
interface ResourceCreator<T extends Resource> {

	/**
	 * The type of resource we create
	 * @return
	 */
	Class<T> type();
	
	/**
	 * convert the given basename and set of arguments into a path.  this
	 * path is used to key the resulting resource in the cache, and is the
	 * key the file watcher uses to notify of a reload, so your create method
	 * should be using this method to do its job
	 * @param baseName
	 * @param args
	 * @return
	 */
	Path toPath(final String baseName, final Object...args);
	
	/**
	 * create the given resource
	 * @param baseName
	 * @param args
	 * @return
	 * @throws IOException
	 */
	T create(final String baseName, final Object...args) throws IOException;
}
