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
package jj.resource;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import jj.configuration.AppLocation;
import jj.configuration.Application;
import jj.configuration.Configuration;
import jj.configuration.PathResolver;
import jj.logging.EmergencyLog;
import jj.resource.stat.ic.StaticResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInstanceCreatorTest  {
	
	// TODO kill this off.  too much concrete stuff
	public static ResourceInstanceCreator creator(
		final PathResolver app,
		final Configuration configuration,
		final EmergencyLog logger
	) {
		return new ResourceInstanceCreator(app, Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(Configuration.class).toInstance(configuration);
				bind(EmergencyLog.class).toInstance(logger);
			}
		}), logger);
	}
	
	@Mock Application app;
	@Mock Injector injector;
	
	@Captor ArgumentCaptor<AbstractModule> moduleCaptor;
	
	@InjectMocks ResourceInstanceCreator rimc;
	
	@Mock ResourceKey cacheKey;
	
	@Before
	public void before() {
		given(injector.createChildInjector(any(AbstractModule.class))).willReturn(injector);
	}
	
	@Test
	public void testPathCreation() {
		
		String name = "name";
		Path path = Paths.get("/");
		
		given(app.resolvePath(AppLocation.Base, name)).willReturn(path);
		
		rimc.createResource(StaticResource.class, cacheKey, AppLocation.Base, name);
		
		verify(app).resolvePath(AppLocation.Base, name);
		verify(injector).createChildInjector(moduleCaptor.capture());
		verify(injector).getInstance(StaticResource.class);
		
		Injector testInjector = Guice.createInjector(moduleCaptor.getValue());
		
		assertThat(testInjector.getInstance(ResourceKey.class), is(cacheKey));
		assertThat(testInjector.getInstance(String.class), is(name));
		assertThat(testInjector.getInstance(Path.class), is(path));
	}
	
	public static class TestResource implements Resource {

		@Override
		public AppLocation base() {
			return null;
		}
		
		@Override
		public String name() {
			return null;
		}

		@Override
		public String uri() {
			return null;
		}

		@Override
		public String sha1() {
			return null;
		}

		@Override
		public void addDependent(Resource dependent) {
		}
		
	}
	
	@Test
	public void testVirtualCreationAndArgs() {
		
		final String name = "name";
		final Integer one = Integer.valueOf(1);
		final Date date = new Date();
		
		rimc.createResource(TestResource.class, cacheKey, AppLocation.Virtual, name, one, date);
		
		verify(app).resolvePath(AppLocation.Virtual, name);
		verify(injector).createChildInjector(moduleCaptor.capture());
		verify(injector).getInstance(TestResource.class);
		
		Injector testInjector = Guice.createInjector(moduleCaptor.getValue());
		
		assertThat(testInjector.getInstance(ResourceKey.class), is(cacheKey));
		assertThat(testInjector.getInstance(String.class), is(name));
		assertThat(testInjector.getExistingBinding(Key.get(Path.class)), is(nullValue()));
		assertThat(testInjector.getInstance(Integer.class), is(one));
		assertThat(testInjector.getInstance(Date.class), is(date));
	}
}
