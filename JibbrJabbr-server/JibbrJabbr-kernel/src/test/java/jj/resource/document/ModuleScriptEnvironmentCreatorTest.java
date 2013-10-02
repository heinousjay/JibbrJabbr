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
package jj.resource.document;

import static org.mockito.BDDMockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

import java.nio.file.Path;

import org.junit.Test;
import org.mockito.Mock;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.resource.ResourceBase;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceInstanceCreator;
import jj.resource.ResourceMaker;
import jj.script.MockRhinoContextMaker;

/**
 * this test only validates that the creator is getting all of the necessary stuff to the resource. there needs to
 * be a separate test for creating a resource to validate it works as expected
 * 
 * @author jason
 *
 */
public class ModuleScriptEnvironmentCreatorTest extends ResourceBase<ModuleScriptEnvironment, ModuleScriptEnvironmentCreator> {

	ResourceMaker resourceMaker;
	@Mock ResourceFinder resourceFinder;
	@Mock EngineAPI api;
	@Mock ScriptableObject local;
	@Mock Publisher publisher;
	@Mock ModuleParent parent;
	MockRhinoContextMaker contextMaker;
	
	@Mock ResourceInstanceCreator mockCreator;
	

	@Override
	protected String baseName() {
		return "helpers";
	}
	
	@Override
	protected Object[] args() {
		
		return new Object[] {parent};
	}
	
	private void givenMinimalServices() throws Exception {
		resourceMaker = new ResourceMaker(configuration);
		
		contextMaker = new MockRhinoContextMaker();
		given(contextMaker.context.newObject(any(Scriptable.class))).willReturn(local);
	}
	
	private void givenModuleScriptEnvironmentResources(String baseName) throws Exception {
		
		ScriptResource serverResource = resourceMaker.makeScript(ScriptResourceType.Module.suffix(baseName));
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Module.suffix(baseName))).willReturn(serverResource);
	}

	@Override
	protected ModuleScriptEnvironment resource() throws Exception {
		
		givenMinimalServices();
		givenModuleScriptEnvironmentResources("helpers");
		
		ModuleScriptEnvironment result = new ModuleScriptEnvironment(cacheKey(), baseName(), parent, publisher, contextMaker, api, resourceFinder);
		
		return result;
	}

	@Override
	protected ModuleScriptEnvironmentCreator toTest() {
		return new ModuleScriptEnvironmentCreator(creator);
	}
	
	@Test
	public void testUri() {
		
		ScriptEnvironment se = mock(ScriptEnvironment.class);
		given(se.baseName()).willReturn("parent");
		
		ModuleScriptEnvironmentCreator msec = toTest();
		
		assertThat(msec.uri("baseName", new ModuleParent(se)).toString(), is("parent#baseName"));
	}
	
	@Test
	public void testRequiresModuleParentForCreation() throws Exception {
		
		boolean caught = false;
		try {
			toTest().create(baseName());
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(true));
		
		caught = false;
		try {
			toTest().create(baseName(), new Object());
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(true));
		
		caught = false;
		try {
			toTest().create(baseName(), new ModuleParent(mock(ScriptEnvironment.class)));
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(false));
		
		caught = false;
		try {
			toTest().create(baseName(), new ModuleParent(mock(ScriptEnvironment.class)), new Object());
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(true));
		
	}
	
	@Test
	public void testCreationHandlesArguments() throws Exception {
		new ModuleScriptEnvironmentCreator(mockCreator).create(baseName(), parent);
		verify(mockCreator).createResource(eq(ModuleScriptEnvironment.class), any(ResourceCacheKey.class), eq(baseName()), any(Path.class), eq(parent));
	}

}
