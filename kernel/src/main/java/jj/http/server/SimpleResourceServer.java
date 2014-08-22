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

import static jj.configuration.resolution.AppLocation.Base;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.netty.handler.codec.http.HttpHeaders;
import jj.http.uri.URIMatch;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoader;
import jj.resource.ServableResource;

/**
 * <p>
 * Handles basic serving of resources that don't require dealing with
 * route mappings. This only reads the path from the URI match to determine
 * which resource to use.  The Route is ignored
 * 
 * @author jason
 *
 */
@Singleton
public class SimpleResourceServer implements ResourceServer {
	
	private final ResourceFinder resourceFinder;
	private final ResourceLoader resourceLoader;
	
	@Inject
	SimpleResourceServer(final ResourceFinder resourceFinder, final ResourceLoader resourceLoader) {
		this.resourceFinder = resourceFinder;
		this.resourceLoader = resourceLoader;
	}
	
	private ServableResource findResource(final Class<? extends ServableResource> resourceClass, final HttpServerRequest request) {
		return resourceFinder.findResource(resourceClass, Base, request.uriMatch().path); // should be Public
	}

	@Override
	public void serve(final Class<? extends ServableResource> resourceClass, final HttpServerRequest request, final HttpServerResponse response) {
		
		// need to get the resource name from the route match
		// for now using the path!
		ServableResource resource = findResource(resourceClass, request);
		
		if (resource == null) {
			resourceLoader.loadResource(resourceClass,  Base, request.uriMatch().path).then(
				new HttpServerTask("serving a resource.  better name!") {
					
					@Override
					protected void run() throws Exception {
						ServableResource resource = findResource(resourceClass, request);
						serve(resource, request, response);
					}
				}
			);
		} else {
			serve(resource, request, response);
		}
		
	}
	
	private void serve(final ServableResource resource, final HttpServerRequest request, final HttpServerResponse response) {
		URIMatch match = request.uriMatch();
		try {
			
			// if we get nothing, they get nothing
			if (resource == null) {
				
				response.sendNotFound();
			}
			
			// if the e-tag matches our SHA, 304
			else if (request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH) &&
				resource.sha1().equals(request.header(HttpHeaders.Names.IF_NONE_MATCH))) {
				
				response.sendNotModified(resource, match.versioned);
			} 

			// if the URI was versioned, we send a cacheable resource if
			// there was no SHA in the URL, or the SHA matches the resource
			else if (
				match.versioned && 
				(match.sha1 == null || match.sha1.equals(resource.sha1()))
			) {
				
				response.sendCachableResource(resource);
			} 

			
			// if the URI was versioned with a SHA that doesn't match our
			// resource, redirect to the right URI
			else if (match.versioned) {
				
				response.sendTemporaryRedirect(resource);
			} 

			
			// if the URI was not versioned, respond with an uncached resource
			// (but with proper e-tags, if we loaded the resource
			else {
				
				response.sendUncachableResource(resource);
				
			}
		
		} catch (Exception e) {
			response.error(e);
		}
	}

}
