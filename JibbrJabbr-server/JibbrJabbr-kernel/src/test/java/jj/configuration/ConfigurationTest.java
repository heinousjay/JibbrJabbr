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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import jj.configuration.Configuration;
import jj.conversion.Converter;
import jj.conversion.ConverterSetMaker;
import jj.conversion.Converters;
import jj.http.server.HttpServerSocketConfiguration;
import jj.http.server.servable.document.DocumentConfiguration;
import jj.logging.EmergencyLogger;
import jj.resource.ConfigResource;
import jj.resource.ConfigResourceMaker;
import jj.resource.ResourceFinder;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.EcmaError;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

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
	
	
	Path realPath;
	String argument;
	Configuration toTest;
	ConfigurationClassLoader classLoader;
	Set<Converter<?, ?>> converterSet;
	Converters converters;
	@Mock ResourceFinder resourceFinder;
	@Mock Logger logger;

	@Before 
	public void before() throws Exception {

		converterSet = ConverterSetMaker.converters();
		converters = new Converters(converterSet);
		
		classLoader = new ConfigurationClassLoader();
		realPath = Paths.get(getClass().getResource("/index.html").toURI()).getParent().toAbsolutePath();
		argument = "app=" + realPath.toString();
		given(resourceFinder.findResource(ConfigResource.class, ConfigResource.CONFIG_JS)).willReturn(ConfigResourceMaker.configResource());
	}
	
	private Injector makeInjector(final String[] args) {
		return Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(String[].class).toInstance(args);
				bind(ResourceFinder.class).toInstance(resourceFinder);
				bind(new TypeLiteral<Set<Converter<?, ?>>>() {}).toInstance(converterSet);
				bind(Logger.class).annotatedWith(EmergencyLogger.class).toInstance(logger);
			}
		});
	}
	
	private Configuration config() throws Exception {
		return config(new String[0]);
	}
	
	private Configuration config(String[] args) throws Exception {
		
		return new Configuration(new Arguments(args), converters, classLoader, makeInjector(args));
	}
	
	@Test
	public void testAppPath() throws Exception {
		toTest = config(new String[]{
			argument
		});
		
		assertThat(toTest.appPath(), is(realPath));
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
