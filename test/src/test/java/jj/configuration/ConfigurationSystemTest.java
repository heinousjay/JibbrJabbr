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
package jj.configuration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static io.netty.handler.codec.http.HttpMethod.*;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.App;
import jj.http.server.Binding;
import jj.http.server.HttpServerSocketConfiguration;
import jj.document.DocumentConfiguration;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.script.ScriptError;
import jj.testing.JibbrJabbrTestServer;
import jj.uri.Route;
import jj.uri.RouterConfiguration;

import org.junit.Rule;
import org.junit.Test;

/**
 * validates a test configuration is properly read
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
public class ConfigurationSystemTest {
	
	static String httpServerSocket(String key) {
		return HttpServerSocketConfiguration.class.getName() + "." + key;
	}
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(App.configuration).injectInstance(this);
	
	@Inject
	private ConfigurationCollector collector;
	
	@Inject
	private DocumentConfiguration documentConfiguration;
	
	@Inject 
	private RouterConfiguration routerConfiguration;
	
	boolean loaded;
	boolean failed;
	
	@Listener 
	void ConfigurationLoaded(ConfigurationLoaded configurationLoaded) {
		loaded = true;
	}
	
	@Listener
	void scriptError(ScriptError scriptError) {
		failed = true;
	}

	@Test
	public void test() throws Exception {
		assert loaded: "didn't load";
		assert !failed : "failed";
		
		// let's peek into the collector to assert some stuff
		assertThat(collector.get(httpServerSocket("keepAlive"), boolean.class, "false"), is(true));
		assertThat(collector.get(httpServerSocket("backlog"), int.class, "0"), is(1024));
		assertThat(collector.get(httpServerSocket("bindings"), List.class, null), isA(List.class));
		@SuppressWarnings("unchecked")
		List<Binding> bindings = (List<Binding>)collector.get(httpServerSocket("bindings"), List.class, null);
		assertThat(bindings.size(), is(2));
		assertThat(bindings.get(0).host(), is(nullValue()));
		assertThat(bindings.get(0).port(), is(8080));
		assertThat(bindings.get(1).host(), is("localhost"));
		assertThat(bindings.get(1).port(), is(8090));
		
		
		// okay good enough.  NOW! make sure that injected configuration is also correct
		assertThat(documentConfiguration.clientDebug(), is(true));
		assertThat(documentConfiguration.showParsingErrors(), is(true));
		assertThat(documentConfiguration.removeComments(), is(false));
		
		// DO MOAR! but just basic stuff
		List<Route> routes = routerConfiguration.routes();
		assertThat(routes.size(), is(8));
		assertThat(routes.get(0).method(), is(GET));
		assertThat(routes.get(1).method(), is(POST));
		assertThat(routes.get(2).method(), is(PUT));
		assertThat(routes.get(3).method(), is(DELETE));
		assertThat(routes.get(4).method(), is(GET));
		assertThat(routes.get(5).method(), is(POST));
		assertThat(routes.get(6).method(), is(PUT));
		assertThat(routes.get(7).method(), is(DELETE));
	}
}
