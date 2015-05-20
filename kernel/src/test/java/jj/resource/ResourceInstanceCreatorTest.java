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
import static org.mockito.Mockito.any;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import jj.application.AppLocation;
import jj.application.Application;
import jj.event.MockPublisher;
import jj.event.Publisher;
import jj.http.server.resource.StaticResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInstanceCreatorTest  {
	
	@Mock PathResolver app;
	@Mock Location location;
	@Mock Injector injector;
	MockPublisher publisher;
	
	@Captor ArgumentCaptor<AbstractModule> moduleCaptor;
	
	ResourceInstanceCreator rimc;
	
	@Mock ResourceKey cacheKey;
	
	@Before
	public void before() {
		rimc = new ResourceInstanceCreator(app, injector, publisher = new MockPublisher());
		given(injector.createChildInjector(any(AbstractModule.class))).willReturn(injector);
	}
	
	@Test
	public void testPathCreation() {
		
		final String name = "name";
		Path path = Paths.get("/");
		
		given(location.representsFilesystem()).willReturn(true);
		given(app.resolvePath(location, name)).willReturn(path);
		
		rimc.createResource(StaticResource.class, cacheKey, location, name);
		
		verify(app).resolvePath(location, name);
		verify(injector).createChildInjector(moduleCaptor.capture());
		verify(injector).getInstance(StaticResource.class);
		
		Injector testInjector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(Application.class).toInstance(mock(Application.class)); // ugly
				bind(Publisher.class).toInstance(mock(Publisher.class));
				bind(ResourceFinder.class).toInstance(mock(ResourceFinder.class));
				bind(ResourceConfiguration.class).toInstance(mock(ResourceConfiguration.class));
			}
		}, moduleCaptor.getValue());
		
		assertThat(testInjector.getInstance(ResourceKey.class), is(cacheKey));
		assertThat(testInjector.getInstance(Key.get(String.class, ResourceName.class)), is(name));
		assertThat(testInjector.getInstance(Path.class), is(path));
	}
	
	public static class TestResource implements Resource {

		@Override
		public Location base() {
			return null;
		}
		
		@Override
		public String name() {
			return null;
		}

		@Override
		public URI uri() {
			return URI.create("");
		}

		@Override
		public String sha1() {
			return null;
		}
		
		@Override
		public ResourceKey cacheKey() {
			return null;
		}

		@Override
		public void addDependent(Resource dependent) {
		}

		@Override
		public Charset charset() {
			return null;
		}
		
		@Override
		public boolean alive() {
			return true;
		}
		
	}
	
	final String name = "name";
	final Integer one = Integer.valueOf(1);
	final Date date = new Date();
	
	@Test
	public void testVirtualCreationAndArgs() {
		
		rimc.createResource(TestResource.class, cacheKey, AppLocation.Virtual, name, date);
		
		verify(injector).createChildInjector(moduleCaptor.capture());
		verify(injector).getInstance(TestResource.class);
		
		Injector testInjector = Guice.createInjector(moduleCaptor.getValue());
		
		assertThat(testInjector.getInstance(ResourceKey.class), is(cacheKey));
		assertThat(testInjector.getInstance(Key.get(String.class, ResourceName.class)), is(name));
		assertThat(testInjector.getExistingBinding(Key.get(Path.class)), is(nullValue()));
		assertThat(testInjector.getInstance(Date.class), is(date));
	}
	
	@Test
	public void testCreationError() {
		
		given(injector.getInstance(TestResource.class)).willThrow(new RuntimeException());
		rimc.createResource(TestResource.class, cacheKey, AppLocation.Virtual, name, one);
		
		assertThat(publisher.events.size(), is(1));
		ResourceError re = (ResourceError)publisher.events.get(0);
		assertThat(re.resourceKey, is(nullValue()));
		assertThat(re.resourceClass, equalTo(TestResource.class));
		assertThat(re.base, is(AppLocation.Virtual));
		assertThat(re.name, is(name));
		assertThat(re.arguments[0], is(one));
	}
}
