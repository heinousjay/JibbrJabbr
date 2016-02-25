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
package jj.http.server;

import jj.resource.Resource;

/**
 * <p>
 * A resource that the server can send to a client. LoadedResource
 * and TransferableResource are the best place to implement this,
 * unless your RouteProcessor handles the transfer to the response,
 * which is totally allowed
 * 
 * <p>
 * Resource classes that implement this interface can be annotated
 * with {@link ServableResourceConfiguration} to provide configuration, although
 * there are reasonable defaults
 * @author jason
 *
 */
public interface ServableResource extends Resource<Void> {


	default String serverPath() {
		return "/" + sha1() + "/" + name();
	}

	default boolean safeToServe() {
		return base().servable();
	}
	
	/**
	 * The mime of resource suitably formatted for response
	 * in the Content-Type header (specifically, including a
	 * charset parameter if needed.)
	 */
	String contentType();
	
	boolean compressible();
}
