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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;

/**
 * Collects routing configuration
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
				for (Route route: configuration.routes()) {
					newTrie.addRoute(route);
				}
				
				// default routes. this should get smarter!
				newTrie.addRoute(new Route(GET, "/*path", ""));
				newTrie.addRoute(new Route(GET, "/", "/" + configuration.welcomeFile()));
				
				newTrie.compress();
				trie.set(newTrie);
			}
		});
	}
	
	public RouteMatch matchURI(final HttpMethod method, final String uri) {
		RouteTrie routes = trie.get();
		assert routes != null : "can't route without configuration!";
		
		Path path = uri.startsWith("/") ? Paths.get(uri) : Paths.get("/" + uri);
		
		return routes.find(method, path.normalize().toAbsolutePath().toString());
	}
}
