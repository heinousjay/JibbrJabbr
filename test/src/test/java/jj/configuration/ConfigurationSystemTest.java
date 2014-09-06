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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.App;
import jj.http.server.Binding;
import jj.http.server.HttpServerSocketConfiguration;
import jj.http.uri.Route;
import jj.http.uri.RouterConfiguration;
import jj.i18n.I18NConfiguration;
import jj.css.LessConfiguration;
import jj.document.DocumentConfiguration;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceConfiguration;
import jj.resource.ResourceSettings;
import jj.script.ScriptError;
import jj.testing.JibbrJabbrTestServer;

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
	
	@Inject
	private LessConfiguration lessConfiguration;
	
	@Inject
	private ResourceConfiguration resourceConfiguration;
	
	@Inject
	private I18NConfiguration i18nConfiguration;
	
	volatile boolean loaded;
	final CountDownLatch loadedLatch = new CountDownLatch(1);
	volatile boolean failed;
	
	@Listener 
	void ConfigurationLoaded(ConfigurationLoaded configurationLoaded) {
		loaded = true;
		loadedLatch.countDown();
	}
	
	@Listener
	void scriptError(ScriptError scriptError) {
		failed = true;
	}

	@Test
	public void test() throws Exception {
		if (!loaded) {
			assertTrue("timed out waiting for load", loadedLatch.await(500, MILLISECONDS));
		}
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
		
		
		assertThat(routerConfiguration.welcomeFile(), is("root"));
		List<Route> routes = routerConfiguration.routes();
		assertThat(routes.size(), is(4));
		assertRoute(routes.get(0), GET, "/chat/", "document", "/chat/list");
//		route.GET('/chat/').to('/chat/list');
//		route.POST('/chat/:room').to('/chat/room');
//		route.PUT('/chat/:room/*secret').to('/chat/room');
//		route.DELETE('/chat/:room/*secret').to('/chat/room');
		
		assertTrue(lessConfiguration.cleancss());
		assertThat(lessConfiguration.optimization(), is(2));
		assertThat(lessConfiguration.maxLineLen(), is(1024));
		
		assertThat(resourceConfiguration.maxFileSizeToLoad(), is(102400000000L));
		assertFalse(resourceConfiguration.watchFiles());
		
		// just some basic checks here
		assertThat(resourceConfiguration.fileTypeSettings().size(), is(42));
		
		ResourceSettings rs = resourceConfiguration.fileTypeSettings().get("html");
		assertThat(rs, is(notNullValue()));
		assertThat(rs.mimeType(), is("text/html"));
		assertThat(rs.charset(), is(UTF_8));
		assertThat(rs.compressible(), is(true));
		
		rs = resourceConfiguration.fileTypeSettings().get("mp4");
		assertThat(rs, is(notNullValue()));
		assertThat(rs.mimeType(), is("video/mp4"));
		assertThat(rs.charset(), is(nullValue()));
		assertThat(rs.compressible(), is(false));
		
		rs = resourceConfiguration.fileTypeSettings().get("stupid");
		assertThat(rs, is(notNullValue()));
		assertThat(rs.mimeType(), is("text/stupid"));
		assertThat(rs.charset(), is(nullValue()));
		assertThat(rs.compressible(), is(false));
		
		
		assertThat(resourceConfiguration.defaultSettings().mimeType(), is("application/octet-stream"));
		
		assertThat(i18nConfiguration.allowNonISO(), is(true));
		assertThat(i18nConfiguration.defaultLocale(), is(Locale.UK));
	}
	
	private void assertRoute(Route route, HttpMethod method, String uri, String resourceName, String mappedName) {
		assertThat(route.method(), is(method));
		assertThat(route.uri(), is(uri));
		assertThat(route.resourceName(), is(resourceName));
		assertThat(route.mappedName(), is(mappedName));
	}
}
