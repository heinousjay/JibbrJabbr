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

import static jj.server.ServerLocation.Virtual;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Date;

import com.google.inject.*;
import jj.application.Application;
import jj.event.MockPublisher;
import jj.event.Publisher;
import jj.http.server.resource.StaticResource;

import jj.util.MockClock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInstanceCreatorTest  {

	private static final Key<ResourceIdentifier<?, ?>> RESOURCE_IDENTIFIER =
		Key.get(new TypeLiteral<ResourceIdentifier<?, ?>>() {});
	
	@Mock PathResolver pathResolver;
	@Mock Location location;
	@Mock Injector injector;
	MockPublisher publisher;
	
	@Captor ArgumentCaptor<AbstractModule> moduleCaptor;
	
	ResourceInstanceCreator rimc;
	
	@Before
	public void before() {
		rimc = new ResourceInstanceCreator(pathResolver, injector, publisher = new MockPublisher());
		given(injector.createChildInjector(any(AbstractModule.class))).willReturn(injector);
	}
	
	@Test
	public void testPathCreation() {
		
		final String name = "name";
		Path path = Paths.get("/");
		
		given(pathResolver.resolvePath(location, name)).willReturn(path);

		ResourceIdentifier<StaticResource, Void> identifier =
			new MockResourceIdentifierMaker().make(StaticResource.class, location, name);
		rimc.createResource(identifier);
		
		verify(pathResolver).resolvePath(location, name);
		verify(injector).createChildInjector(moduleCaptor.capture());
		verify(injector).getInstance(StaticResource.class);
		
		Injector testInjector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(Application.class).toInstance(mock(Application.class)); // ugly
				bind(PathResolver.class).toInstance(pathResolver);
				bind(Clock.class).toInstance(new MockClock());
				bind(Publisher.class).toInstance(mock(Publisher.class));
				bind(ResourceFinder.class).toInstance(mock(ResourceFinder.class));
				bind(ResourceConfiguration.class).toInstance(mock(ResourceConfiguration.class));
			}
		}, moduleCaptor.getValue());

		assertThat(testInjector.getInstance(RESOURCE_IDENTIFIER), is(identifier));
		assertThat(testInjector.getInstance(Path.class), is(path));
	}

	final String name = "name";
	final Date date = new Date();
	
	@Test
	public void testVirtualCreationAndArgs() {

		ResourceIdentifier<TestDateResource, Date> identifier =
			new MockResourceIdentifierMaker().make(TestDateResource.class, Virtual, name, date);
		rimc.createResource(identifier);
		
		verify(injector).createChildInjector(moduleCaptor.capture());
		verify(injector).getInstance(TestDateResource.class);
		
		Injector testInjector = Guice.createInjector(moduleCaptor.getValue());
		
		assertThat(testInjector.getInstance(RESOURCE_IDENTIFIER), is(identifier));
		assertThat(testInjector.getExistingBinding(Key.get(Path.class)), is(nullValue()));
		assertThat(testInjector.getInstance(Date.class), is(date));
	}
	
	@Test
	public void testCreationError() {
		
		given(injector.getInstance(TestDateResource.class)).willThrow(new RuntimeException());
		ResourceIdentifier<TestDateResource, Date> identifier =
			new MockResourceIdentifierMaker().make(TestDateResource.class, Virtual, name, date);
		rimc.createResource(identifier);
		
		assertThat(publisher.events.size(), is(1));
		ResourceError re = (ResourceError)publisher.events.get(0);
		assertThat(re.identifier, is((Object)identifier));
	}
}
