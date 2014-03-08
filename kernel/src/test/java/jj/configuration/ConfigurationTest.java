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
import static org.mockito.BDDMockito.*;
import jj.configuration.Configuration;
import jj.conversion.ConverterSetMaker;
import jj.conversion.Converters;
import jj.document.servable.DocumentConfiguration;
import jj.http.server.HttpServerSocketConfiguration;
import jj.logging.SystemLogger;
import jj.resource.ResourceFinder;
import jj.resource.config.ConfigResource;
import jj.resource.config.ConfigResourceMaker;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.EcmaError;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {
	
	public interface Fails {}
	
	public interface NotAConfiguration {
		
		char otherThing();
		byte something();
		short zero();
		boolean one();
		int two();
		long three();
		float four();
		double five();
		Object six();
	}
	
	
	private Configuration toTest;
	private ConfigurationClassLoader classLoader;
	private @Mock ResourceFinder resourceFinder;
	private @Mock SystemLogger logger;

	@Before 
	public void before() throws Exception {
		
		classLoader = new ConfigurationClassLoader();
		given(resourceFinder.findResource(ConfigResource.class, AppLocation.Base, ConfigResource.CONFIG_JS)).willReturn(ConfigResourceMaker.configResource());
	}
	
	private Injector makeInjector() {
		return Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(Converters.class).toInstance(new Converters(ConverterSetMaker.converters()));
				bind(ResourceFinder.class).toInstance(resourceFinder);
				bind(SystemLogger.class).toInstance(logger);
			}
		});
	}
	
	private Configuration config() throws Exception {
		
		return new Configuration(classLoader, makeInjector());
	}
	
	@Test
	public void testConfigurationObjectInstanceIsNotTheSame() throws Exception {
		toTest = config();
		
		HttpServerSocketConfiguration instance1 = toTest.get(HttpServerSocketConfiguration.class);
		HttpServerSocketConfiguration instance2 = toTest.get(HttpServerSocketConfiguration.class);
		
		assertThat(instance1, is(not(sameInstance(instance2))));
	}
	
	@Test
	public void testErrorIsLogged() throws Exception {
		toTest = config();
		
		EcmaError error = null;
		try {
			toTest.get(Fails.class);
			fail("should have failed");
		} catch (EcmaError e) {
			error = e;
		}
		assertThat(error, is(notNullValue()));
		assertThat(error.getMessage(), Matchers.startsWith("TypeError: Cannot find function fail in object [object Object]."));
		verify(logger).error(anyString(), eq(error.getMessage()), eq(error.getScriptStackTrace()));
	}
	
	@Test
	public void testConfigurationObject() throws Exception {
		toTest = config();
		
		HttpServerSocketConfiguration config = toTest.get(HttpServerSocketConfiguration.class);
		assertThat(config.keepAlive(), is(true));
		assertThat(config.reuseAddress(), is(true));
		assertThat(config.tcpNoDelay(), is(true));
		assertThat(config.backlog(), is(1024));
		assertThat(config.timeout(), is(10000));
		assertThat(config.sendBufferSize(), is(65536));
		assertThat(config.receiveBufferSize(), is(65536));
		assertThat(config.bindings(), is(notNullValue()));
		assertThat(config.bindings().length, is(2));
		assertThat(config.bindings()[0].host(), is(nullValue()));
		assertThat(config.bindings()[0].port(), is(8080));
		assertThat(config.bindings()[1].host(), is("localhost"));
		assertThat(config.bindings()[1].port(), is(8090));
		
		DocumentConfiguration docConfig = toTest.get(DocumentConfiguration.class);
		assertThat(docConfig.clientDebug(), is(false));
		assertThat(docConfig.removeComments(), is(true));
		assertThat(docConfig.showParsingErrors(), is(false));
		
		NotAConfiguration notAConfig = toTest.get(NotAConfiguration.class);
		assertThat(notAConfig.one(), is(false));
		assertThat(notAConfig.two(), is(0));
	}
	
	@Test
	public void testIsSystemRunning() throws Exception {
		toTest = config();
		assertThat(toTest.isSystemRunning(), is(true));
		assertThat(mock(Configuration.class).isSystemRunning(), is(false));
	}

}
