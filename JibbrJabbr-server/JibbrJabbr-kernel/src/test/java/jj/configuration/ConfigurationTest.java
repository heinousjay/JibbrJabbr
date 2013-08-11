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
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.configuration.Configuration;
import jj.resource.ResourceFinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {
	
	static final String PATH_ARG = "pathArg";
	static final String BOOL_ARG = "boolArg";
	
	public interface ConfigurationTestInterface {
		
		@Argument(PATH_ARG)
		Path path();
		
		@Argument(BOOL_ARG)
		boolean bool();
	}
	
	public interface DefaultTestInterface {
		
		@Argument("false")
		@Default("false")
		boolean falseBool();
		
		@Argument("true")
		@Default("true")
		boolean trueBool();
	}
	
	Path realPath;
	Configuration toTest;
	ConfigurationClassLoader classLoader;
	@Mock ResourceFinder resourceFinder;

	@Before 
	public void before() throws Exception {
		classLoader = new ConfigurationClassLoader();
		realPath = Paths.get(getClass().getResource("/index.html").toURI()).getParent();
	}
	
	private Injector makeInjector(final String[] args) {
		return Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(String[].class).toInstance(args);
				bind(ResourceFinder.class).toInstance(resourceFinder);
			}
		});
	}
	
	@Test
	public void testRetrieveConfigurationInstances() throws Exception {
		toTest = new Configuration(classLoader, makeInjector(new String[] {
			PATH_ARG + "=" + realPath.toString(),
			BOOL_ARG + "=true"
		}));
		
		ConfigurationTestInterface instance = toTest.get(ConfigurationTestInterface.class);
		
		assertThat(instance, is(notNullValue()));
		
		assertThat(instance.path(), is(realPath));
		
		assertThat(instance.bool(), is(true));
	}
	
	@Test
	public void testDefaultValue() throws Exception {
		toTest = new Configuration(classLoader, makeInjector(new String[] {}));
		
		DefaultTestInterface instance = toTest.get(DefaultTestInterface.class);
		
		assertThat(instance, is(notNullValue()));
		
		assertThat(instance.falseBool(), is(false));
		assertThat(instance.trueBool(), is(true));
	}
	
	@Test
	public void testConfigurationObjectInstanceIsTheSame() throws Exception {
		toTest = new Configuration(classLoader, makeInjector(new String[] {}));
		
		ConfigurationTestInterface instance1 = toTest.get(ConfigurationTestInterface.class);
		ConfigurationTestInterface instance2 = toTest.get(ConfigurationTestInterface.class);
		
		assertThat(instance1, is(sameInstance(instance2)));
	}
	
	@Test
	public void testIsSystemRunning() throws Exception {
		toTest = new Configuration(null, null);
		assertThat(toTest.isSystemRunning(), is(true));
		assertThat(mock(Configuration.class).isSystemRunning(), is(false));
	}

}
