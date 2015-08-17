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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.netty.handler.codec.http.HttpHeaderNames;
import jj.http.server.uri.Route;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.URIMatch;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoader;
import jj.resource.ResourceThread;

/**
 * <p>
 * Serves resources that have simple lifecycle needs
 * 
 * @author jason
 *
 */
@Singleton
public class SimpleRouteProcessor implements RouteProcessor {
	
	private final ResourceFinder resourceFinder;
	private final ResourceLoader resourceLoader;
	private final Map<String, Class<? extends ServableResource>> servableResources;
	private final Map<Class<? extends ServableResource>, RouteProcessorConfiguration> configurationMap;
	
	@Inject
	SimpleRouteProcessor(
		final ResourceFinder resourceFinder,
		final ResourceLoader resourceLoader,
		final Map<String, Class<? extends ServableResource>> servableResources,
		final Map<Class<? extends ServableResource>, RouteProcessorConfiguration> configurationMap
	) {
		this.resourceFinder = resourceFinder;
		this.resourceLoader = resourceLoader;
		this.servableResources = servableResources;
		this.configurationMap = configurationMap;
	}
	
	private ServableResource findResource(final Class<? extends ServableResource> resourceClass, final HttpServerRequest request) {
		return resourceLoader.findResource(resourceClass, configurationMap.get(resourceClass).location(), request.uriMatch().path, null);
	}
	
	@ResourceThread
	@Override
	public <T extends ServableResource> T loadResource(final Class<T> resourceClass, final URIMatch uriMatch, final Route route) {
		return resourceFinder.loadResource(resourceClass, configurationMap.get(resourceClass).location(), uriMatch.path, null);
	}

	@Override
	public void process(final RouteMatch routeMatch, final HttpServerRequest request, final HttpServerResponse response) {
		
		Class<? extends ServableResource> resourceClass = servableResources.get(routeMatch.resourceName());

		assert resourceClass != null : "configured a route processor incorrectly";

		ServableResource resource = findResource(resourceClass, request);

		if (resource == null) {
			// TODO - just use the task runner
			resourceLoader.loadResource(
				resourceClass,
				configurationMap.get(resourceClass).location(),
				request.uriMatch().path,
				null
			).then(
				new HttpServerTask("post-load, serving " + routeMatch.route()) {

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
			else if (resource.sha1().equals(request.header(HttpHeaderNames.IF_NONE_MATCH))) {
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
