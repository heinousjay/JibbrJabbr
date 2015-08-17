package jj.resource;

import java.io.IOException;

/**
 * represents the ability to create a resource given a baseName and
 * some unspecified arguments
 * @author jason
 *
 * @param <T> The type of resource we create
 */
interface ResourceCreator<A, T extends Resource<A>> {

	/**
	 * The type of resource we create
	 * @return
	 */
	Class<T> type();

	/**
	 * Produce a resource key for given resource by its creation args
	 * @return
	 */
	ResourceKey resourceKey(final Location base, final String name, final A argument);
	
	/**
	 * create the given resource
	 * @param baseName
	 * @param args
	 * @return
	 * @throws IOException
	 */
	T create(final Location base, final String name, final A argument) throws IOException;
}
