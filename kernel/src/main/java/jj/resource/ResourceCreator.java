package jj.resource;

import java.io.IOException;
import java.net.URI;

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
	 * Indicates if this creator can create a resource for the
	 * given name and args
	 * @param name
	 * @param args
	 * @return
	 */
	boolean canLoad(final String name, final Object...args);

	/**
	 * Produce a cache key for given resource by its creation args
	 * @return
	 */
	ResourceCacheKey cacheKey(final String baseName, final Object...args);
	
	/**
	 * Produce a cache key for a given resource by its URI
	 */
	ResourceCacheKey cacheKey(final URI uri);
	
	/**
	 * create the given resource
	 * @param baseName
	 * @param args
	 * @return
	 * @throws IOException
	 */
	T create(final String baseName, final Object...args) throws IOException;
}