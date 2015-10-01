/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.resource;

import jj.execution.Promise;

/**
 * <p>
 * Provides an asynchronous interface to loading a resource
 * if you need the fire-and-forget action. Also exposes the find
 * methods to make your life somewhat easier
 *
 * <p>
 * All methods on this service return immediately and perform no
 * I/O, so they are all fine candidates to be called from any tasks
 * 
 * @author jason
 *
 */
public interface ResourceLoader {

	/**
	 * Find the identified resource in the cache, returning null if not loaded
	 */
	<T extends Resource<A>, A> T findResource(ResourceIdentifier<T, A> identifier);

	/**
	 * Find the identified resource in the cache, returning null if not loaded
	 */
	<T extends Resource<Void>> T findResource(Class<T> resourceClass, Location base, String name);

	/**
	 * Find the identified resource in the cache, returning null if not loaded
	 */
	<T extends Resource<A>, A> T findResource(Class<T> resourceClass, Location base, String name, A argument);

	/**
	 * Attempt to load the identified resource asynchronously, returning the task's {@link Promise}
	 */
	<T extends Resource<A>, A> Promise loadResource(ResourceIdentifier<T, A> identifier);

	/**
	 * Attempt to load the identified resource asynchronously, returning the task's {@link Promise}
	 */
	<T extends Resource<Void>> Promise loadResource(Class<T> resourceClass, Location base, String name);

	/**
	 * Attempt to load the identified resource asynchronously, returning the task's {@link Promise}
	 */
	<T extends Resource<A>, A> Promise loadResource(Class<T> resourceClass, Location base, String name, A argument);

}