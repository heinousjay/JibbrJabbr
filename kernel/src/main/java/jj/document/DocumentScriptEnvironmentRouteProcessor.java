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
package jj.document;

import static jj.server.ServerLocation.*;
import static jj.document.DocumentScriptEnvironment.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.document.servable.DocumentRequestProcessor;
import jj.execution.TaskRunner;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.http.server.RouteProcessor;
import jj.http.server.ServableResource;
import jj.http.server.resource.StaticResource;
import jj.http.server.uri.Route;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.URIMatch;
import jj.resource.ResourceFinder;
import jj.resource.ResourceTask;
import jj.resource.ResourceThread;

/**
 * @author jason
 *
 */
@Singleton
public class DocumentScriptEnvironmentRouteProcessor implements RouteProcessor {
	
	private final ResourceFinder resourceFinder;
	private final TaskRunner taskRunner;
	private final Injector parentInjector;
	
	@Inject
	DocumentScriptEnvironmentRouteProcessor(final ResourceFinder resourceFinder, final TaskRunner taskRunner, final Injector parentInjector) {
		this.resourceFinder = resourceFinder;
		this.taskRunner = taskRunner;
		this.parentInjector = parentInjector;
	}

	@Override
	public void process(final RouteMatch routeMatch, final HttpServerRequest request, final HttpServerResponse response) {
		
		Route route = routeMatch.route();
		
		DocumentScriptEnvironment dse = findDocumentScriptEnvironment(route.mapping());
		
		if (dse == null) {
			taskRunner.execute(new ResourceTask("Loading document script at " + route.mapping()) {

				@Override
				protected void run() throws Exception {
					preloadResources();
					DocumentScriptEnvironment dse = resourceFinder.loadResource(DocumentScriptEnvironment.class, Virtual, route.mapping());
					serve(dse, request, response);
				}
				
			});
		} else {
			serve(dse, request, response);
		}
	}
	
	@ResourceThread
	@Override
	public <T extends ServableResource> T loadResource(final Class<T> resourceClass, final URIMatch uriMatch, final Route route) {
		assert resourceClass == DocumentScriptEnvironment.class;
		return resourceFinder.loadResource(resourceClass, Virtual, route.mapping());
	}
	
	private void preloadResources() {
		// since we're in the IO thread already and we might need this stuff soon, as a small
		// optimization to avoid jumping right back into the I/O thread after dispatching this
		// into the script thread, we just "prime the pump"
		resourceFinder.loadResource(StaticResource.class, Assets, JJ_JS);
		resourceFinder.loadResource(StaticResource.class, Assets, JQUERY_JS);
	}

	private DocumentScriptEnvironment findDocumentScriptEnvironment(String name) {
		return resourceFinder.findResource(DocumentScriptEnvironment.class, Virtual, name);
	}
	
	private void serve(DocumentScriptEnvironment dse, HttpServerRequest request, HttpServerResponse response) {
		if (dse == null) {
			
			response.sendNotFound();
			
		} else if (dse.initializationDidError()) {
				
			response.error(dse.initializationError());
			
		} else {
			parentInjector.createChildInjector(new AbstractModule() {
				
				@Override
				protected void configure() {
					bind(DocumentScriptEnvironment.class).toInstance(dse);
					bind(HttpServerRequest.class).toInstance(request);
					bind(HttpServerResponse.class).toInstance(response);
				}
			}).getInstance(DocumentRequestProcessor.class).process();
		}
	}

}
