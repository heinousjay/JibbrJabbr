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

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.configuration.resolution.AppLocation.Virtual;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.CONFIG_SCRIPT_NAME;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.App;
import jj.http.server.HttpServerSocketConfiguration;
import jj.configuration.ConfigurationScriptEnvironment.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceLoader;
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
@Subscriber
public class ConfigurationSystemTest {
	
	static String httpServerSocket(String key) {
		return HttpServerSocketConfiguration.class.getName() + "." + key;
	}
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(App.one).injectInstance(this);
	
	@Inject
	private ResourceLoader resourceFinder;
	
	@Inject
	private ConfigurationCollector collector;
	
	private CountDownLatch latch;
	
	@Listener
	void configLoaded(ConfigurationLoaded event) {
		if (latch != null) latch.countDown();
	}

	@Test
	public void test() throws Exception {
		latch = new CountDownLatch(1);
		
		resourceFinder.loadResource(ConfigurationScriptEnvironment.class, Virtual, CONFIG_SCRIPT_NAME);
		
		assertTrue(latch.await(2, SECONDS));
		
		// and let's peek into the collector to assert some stuff
		assertThat(collector.get(httpServerSocket("keepAlive"), boolean.class, "false"), is(true));
		assertThat(collector.get(httpServerSocket("backlog"), int.class, "0"), is(1024));
	}
}
