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
package jj.http.uri;

import static io.netty.handler.codec.http.HttpMethod.*;

import io.netty.handler.codec.http.HttpMethod;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;

/**
 * The public interface to the routing system.  mainly just mediates
 * between configuration loading and requests into the system
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
public class Router {

	private final RouterConfiguration configuration;
	private final TaskRunner taskRunner;
	private final AtomicReference<RouteTrie> trie = new AtomicReference<>();
	
	@Inject
	Router(
		final RouterConfiguration configuration,
		final TaskRunner taskRunner
	) {
		this.configuration = configuration;
		this.taskRunner = taskRunner;
	}
	
	@Listener
	void configurationLoaded(ConfigurationLoaded configurationLoaded) {
		taskRunner.execute(new ServerTask("parsing routing configuration") {
			
			@Override
			protected void run() throws Exception {
				RouteTrie newTrie = new RouteTrie();
				// java 8 rocks!
				configuration.routes().forEach(newTrie::addRoute);
				
				// default routes.
				// this is the only route we add, it is meant to be the StaticResource mapping.
				// since it's the catch-all, it goes last and gets added to every route trie
				newTrie.addRoute(new Route(GET, "/*path", ""));
				
				// this is not for here - this should be handled by rewriting the incoming match
				// to have the welcome file in it if it matches a DirectoryResource
				newTrie.addRoute(new Route(GET, "/", "/" + configuration.welcomeFile()));
				
				newTrie.compress();
				trie.set(newTrie);
			}
		});
	}
	
	public RouteMatch matchURI(final HttpMethod method, final URIMatch uriMatch) {
		
		// this is not the correct signature? RouteMatch is the wrong return, needs to
		// be some object that indicates how to do the processing
		
		// okay, this has a few jobs to do.  let's detail them!
		// if the incoming URI had a / on the end (except '/'), issue a redirect without it
		// if uriMatch.path is a DirectoryResource, rewrite the URIMatch to have the welcome file on it
		// hit the routes and see what we get back out of it
		
		// not sure if this is the right place for that process. maybe whatever calls this class should do
		// the first two steps
		
		RouteTrie routes = trie.get();
		assert routes != null : "can't route before configuration is complete!";
		
		return routes.find(method, uriMatch);
	}
}
