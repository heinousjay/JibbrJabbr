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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.configuration.resolution.AppLocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * base test for resource testing
 * @author jason
 *
 */
public abstract class ResourceBase<U extends Resource, T extends ResourceCreator<U>> extends RealResourceBase {
	
	
	protected ResourceInstanceCreator creator;
	@Mock Injector injector;
	
	protected U resource;
	
	protected T toTest;
	
	protected Object[] args() {
		return AbstractFileResource.EMPTY_ARGS;
	}
	
	protected AppLocation location() {
		return AppLocation.Base;
	}
	
	protected ResourceKey cacheKey() {
		return toTest.resourceKey(location(), name(), args());
	}
	
	protected abstract String name();
	
	protected abstract U resource() throws Exception;
	
	protected abstract T toTest();
	
	protected void before() throws Exception {}

	@SuppressWarnings("unchecked")
	protected void configureInjector(U resource) {
		given(injector.getInstance(any(Class.class))).willReturn(resource);
	}
	
	@Before
	public final void setup() throws Exception {
		
		before();
		
		creator = new ResourceInstanceCreator(app, injector, logger);
		
		toTest = toTest();
		
		resource = resource();
		
		given(injector.createChildInjector(any(Module.class))).willReturn(injector);
		configureInjector(resource);
	}
	
	@Test
	public void test() throws Exception {
		
		U created = toTest.create(location(), name(), args()); 
		
		testResource(created);
		
		assertThat(created, is(resource));
		
		resourceAssertions(created);
		
		verify(logger, never()).error(anyString(), any(ResourceNotViableException.class));
	}
	
	protected void resourceAssertions(U resource) throws Exception {
		
	}
}
