package jj.resource;

import java.io.IOException;

import jj.configuration.AppLocation;

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
	 * Produce a cache key for given resource by its creation args
	 * @return
	 */
	ResourceCacheKey cacheKey(final AppLocation base, final String name, final Object...args);
	
	/**
	 * create the given resource
	 * @param baseName
	 * @param args
	 * @return
	 * @throws IOException
	 */
	T create(final AppLocation base, final String name, final Object...args) throws IOException;
}
