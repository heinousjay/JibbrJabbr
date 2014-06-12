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

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.App;
import jj.http.server.Binding;
import jj.http.server.HttpServerSocketConfiguration;
import jj.document.DocumentConfiguration;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Rule;
import org.junit.Test;

/**
 * uses the default confi
 * 
 * @author jason
 *
 */
@Singleton
public class ConfigurationSystemTest {
	
	static String httpServerSocket(String key) {
		return HttpServerSocketConfiguration.class.getName() + "." + key;
	}
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(App.two).injectInstance(this);
	
	@Inject
	private ConfigurationCollector collector;
	
	@Inject
	private DocumentConfiguration documentConfiguration;

	@Test
	public void test() throws Exception {
		
		// let's peek into the collector to assert some stuff
		assertThat(collector.get(httpServerSocket("keepAlive"), boolean.class, "false"), is(true));
		assertThat(collector.get(httpServerSocket("backlog"), int.class, "0"), is(1024));
		assertThat(collector.get(httpServerSocket("bindings"), List.class, null), isA(List.class));
		@SuppressWarnings("unchecked")
		List<Binding> bindings = (List<Binding>)collector.get(httpServerSocket("bindings"), List.class, null);
		assertThat(bindings.size(), is(2));
		assertThat(bindings.get(0), isA(Binding.class));
		assertThat(bindings.get(1), isA(Binding.class));
		
		// okay good enough.  NOW! make sure that injected configuration is also correct
		assertThat(documentConfiguration.clientDebug(), is(true));
		assertThat(documentConfiguration.showParsingErrors(), is(true));
		assertThat(documentConfiguration.removeComments(), is(false));
		
		// DO MOAR!
	}
}
