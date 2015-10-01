package jj.resource;

import java.io.IOException;

/**
 * represents the ability to create a typed resource given a location, name and
 * possibly an argument
 * @author jason
 *
 * @param <A> The type of argument required for resource creation
 * @param <T> The type of resource we create
 */
interface ResourceCreator<A, T extends Resource<A>> {

	/**
	 * The type of resource we create
	 * @return
	 */
	Class<T> type();
	
	/**
	 * create the given resource, if possible
	 * @param location The server location to check
	 * @param name the resource name
	 * @param argument a specific creation argument, if needed
	 * @return The newly created resource, or null if creation could not proceed
	 * @throws IOException potentially
	 */
	T create(final Location location, final String name, final A argument) throws IOException;
}
