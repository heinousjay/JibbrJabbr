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
package jj.script.module;


import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;
import jj.resource.Location;
import jj.script.PendingKey;
import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.RealRhinoContextProvider;
import jj.script.AbstractScriptEnvironment;
import jj.script.module.ModuleScriptEnvironment;
import jj.script.module.RequiredModule;
import jj.script.module.RootScriptEnvironment;
import jj.script.module.ScriptResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ModuleScriptEnvironmentTest {
	
	RealRhinoContextProvider contextProvider;
	
	RequiredModule requiredModule;
	
	@Mock(extraInterfaces = { RootScriptEnvironment.class }) AbstractScriptEnvironment parent;
	
	@Mock ScriptResource scriptResource;
	
	@Mock JSONResource jsonResource;
	
	@Mock ScriptableObject contents;
	
	ModuleScriptEnvironment mse;
	
	@Mock ScriptableObject global;

	private MockAbstractScriptEnvironmentDependencies construct(String name) {
		contextProvider = new RealRhinoContextProvider();
		
		MockAbstractScriptEnvironmentDependencies dependencies =
			new MockAbstractScriptEnvironmentDependencies(contextProvider, name);
		
		given(((RootScriptEnvironment)parent).global()).willReturn(global);
		
		requiredModule = new RequiredModule((RootScriptEnvironment)parent, name);
		requiredModule.pendingKey(new PendingKey());
		
		given(parent.alive()).willReturn(true);
		return dependencies;
	}
	
	private void givenModuleLocation(Location location) {
		given(((RootScriptEnvironment)parent).moduleLocation()).willReturn(location);
	}

	private void constructScriptModule(String name, String moduleIdentifier, Location scriptLocation) {
		
		MockAbstractScriptEnvironmentDependencies dependencies = construct(name);
		
		given(dependencies.resourceFinder().loadResource(ScriptResource.class, scriptLocation, moduleIdentifier + ".js")).willReturn(scriptResource);
		
		given(scriptResource.base()).willReturn(scriptLocation);
		given(scriptResource.name()).willReturn(moduleIdentifier);
		given(scriptResource.source()).willReturn("");
		
		givenModuleLocation(scriptLocation);
		mse = new ModuleScriptEnvironment(dependencies, requiredModule);
	}

	private void constructJSONModule(String name, String moduleIdentifier, Location jsonLocation) {
		
		MockAbstractScriptEnvironmentDependencies dependencies = construct(name);
		
		given(dependencies.resourceFinder().loadResource(JSONResource.class, jsonLocation, moduleIdentifier + ".json")).willReturn(jsonResource);
		
		given(jsonResource.base()).willReturn(jsonLocation);
		given(jsonResource.name()).willReturn(moduleIdentifier);
		given(jsonResource.contents()).willReturn(contents);
		
		givenModuleLocation(jsonLocation);
		mse = new ModuleScriptEnvironment(dependencies, requiredModule);
	}
	
	@Test
	public void testUserScriptModule() {
		
		constructScriptModule("module", "module", AppBase);
		
		assertThat(mse.scope().get("inject", mse.scope()), is(ScriptableObject.NOT_FOUND));
	}

	@Test
	public void testApiScriptModule() {
		
		constructScriptModule("jj/module", "module", APIModules);
		
		assertThat(mse.scope().get("inject", mse.scope()), is(instanceOf(Function.class)));
	}
	
	@Test
	public void testUserJSONModule() {
		
		constructJSONModule("module", "module", AppBase);
		
		assertThat(mse.exports(), is(contents));
	}

	@Test
	public void testApiJSONModule() {
		
		constructJSONModule("jj/module", "module", APIModules);
		
		assertThat(mse.exports(), is(contents));
	}
}
