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
package jj.http.server.uri;

import static io.netty.handler.codec.http.HttpMethod.*;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import jj.http.server.RouteContributor;

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
	private final Set<RouteContributor> routeContributors;
	private final TaskRunner taskRunner;
	private final AtomicReference<RouteTrie> trie = new AtomicReference<>();
	
	@Inject
	Router(
		final RouterConfiguration configuration,
		final Set<RouteContributor> routeContributors,
		final TaskRunner taskRunner
	) {
		this.configuration = configuration;
		this.routeContributors = routeContributors;
		this.taskRunner = taskRunner;
	}
	
	@Listener
	void on(ConfigurationLoaded configurationLoaded) {
		taskRunner.execute(new ServerTask("parsing routing configuration") {
			
			@Override
			protected void run() throws Exception {
				RouteTrie newTrie = new RouteTrie();
				// java 8 rocks!
				configuration.routes().forEach(newTrie::addRoute);
				
				routeContributors.forEach((contributor) -> contributor.contributions().forEach(newTrie::addRoute));
			
				// this is the catch-all, it goes last and gets added to every route trie
				newTrie.addRoute(new Route(GET, "/*fallthrough", "static", ""));
				
				newTrie.compress();
				
				trie.set(newTrie);
			}
		});
	}
	
	public RouteMatch routeRequest(final HttpMethod method, final URIMatch uriMatch) {
		RouteTrie routes = trie.get();
		assert routes != null : "can't route before configuration is complete!";
		
		return routes.find(method, uriMatch);
	}
}
