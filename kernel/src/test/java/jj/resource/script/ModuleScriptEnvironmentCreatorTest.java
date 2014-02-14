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
package jj.resource.script;

import static org.mockito.BDDMockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

import java.nio.file.Path;

import org.junit.Test;
import org.mockito.Mock;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.engine.EngineAPI;
import jj.resource.AbstractResource;
import jj.resource.ResourceBase;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceInstanceCreator;
import jj.resource.ResourceMaker;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironmentCreator;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.script.ContinuationPendingKey;
import jj.script.MockRhinoContextProvider;
import jj.script.MockableScriptEnvironmentInitializer;
import jj.script.ScriptEnvironment;

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
	RequiredModule requiredModule;
	// lil ugly! but this satisfies things internally
	@Mock(extraInterfaces={ScriptEnvironment.class}) AbstractResource scriptEnvironment;
	MockRhinoContextProvider contextProvider;
	
	@Mock MockableScriptEnvironmentInitializer initializer;
	
	@Mock ResourceInstanceCreator mockCreator;
	
	@Mock Script script;
	

	@Override
	protected String baseName() {
		return "helpers";
	}
	
	@Override
	protected Object[] args() {
		
		return new Object[] {requiredModule};
	}
	
	@Override
	protected void before() throws Exception {
		
		given(resourceFinder.findResource(scriptEnvironment)).willReturn(scriptEnvironment);
	}
	
	private void givenMinimalServices() throws Exception {
		resourceMaker = new ResourceMaker(configuration, arguments);
		
		contextProvider = new MockRhinoContextProvider();
		given(contextProvider.context.newObject(any(Scriptable.class))).willReturn(local);
	}
	
	private void givenModuleScriptEnvironmentResources(String baseName) throws Exception {
		requiredModule = new RequiredModule((ScriptEnvironment)scriptEnvironment, baseName);
		requiredModule.pendingKey(new ContinuationPendingKey());
		ScriptResource scriptResource = resourceMaker.makeScript(ScriptResourceType.Module.suffix(baseName));
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Module.suffix(baseName))).willReturn(scriptResource);
		
		given(contextProvider.context.compileString(anyString(), anyString())).willReturn(script);
	}

	@Override
	protected ModuleScriptEnvironment resource() throws Exception {
		
		givenMinimalServices();
		givenModuleScriptEnvironmentResources("helpers");
		
		ModuleScriptEnvironment result = new ModuleScriptEnvironment(cacheKey(), baseName(), requiredModule, contextProvider, api, resourceFinder);
		
		return result;
	}

	@Override
	protected ModuleScriptEnvironmentCreator toTest() {
		return new ModuleScriptEnvironmentCreator(initializer, creator);
	}
	
	@Override
	protected void resourceAssertions(ModuleScriptEnvironment resource) throws Exception {
		// it was initialized.  yay
		verify(initializer).initializeScript(resource);
	}
	
	@Test
	public void testUri() {
		
		ScriptEnvironment se = mock(ScriptEnvironment.class);
		given(se.baseName()).willReturn("parentUri");
		
		ModuleScriptEnvironmentCreator msec = toTest();
		
		assertThat(msec.uri("baseName", new RequiredModule(se, "baseName")).toString(), is("parentUri#baseName"));
	}
	
	@Test
	public void testRequiresArgumentForCreation() throws Exception {
		
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
			toTest().create(baseName(), new RequiredModule(mock(ScriptEnvironment.class), baseName()));
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(false));
		
		caught = false;
		try {
			toTest().create(baseName(), new RequiredModule(mock(ScriptEnvironment.class), ""), new Object());
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(true));
		
	}
	
	@Test
	public void testCreationHandlesArguments() throws Exception {
		new ModuleScriptEnvironmentCreator(initializer, mockCreator).create(baseName(), requiredModule);
		verify(mockCreator).createResource(eq(ModuleScriptEnvironment.class), any(ResourceCacheKey.class), eq(baseName()), any(Path.class), eq(requiredModule));
	}

}
