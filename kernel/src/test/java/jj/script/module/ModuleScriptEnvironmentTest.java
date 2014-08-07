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


import static jj.configuration.resolution.AppLocation.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;
import jj.resource.Location;
import jj.script.ContinuationPendingKey;
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
	
	ModuleScriptEnvironment mse;
	
	@Mock ScriptableObject global;

	public void construct(String name, String moduleIdentifier, Location scriptBase) {
		
		contextProvider = new RealRhinoContextProvider();
		
		MockAbstractScriptEnvironmentDependencies dependencies =
			new MockAbstractScriptEnvironmentDependencies(contextProvider, name);
		
		given(((RootScriptEnvironment)parent).global()).willReturn(global);
		
		requiredModule = new RequiredModule((RootScriptEnvironment)parent, name);
		requiredModule.pendingKey(new ContinuationPendingKey());
		
		given(parent.alive()).willReturn(true);
		
		given(dependencies.resourceFinder().loadResource(ScriptResource.class, scriptBase, moduleIdentifier + ".js")).willReturn(scriptResource);
		
		given(scriptResource.base()).willReturn(scriptBase);
		given(scriptResource.name()).willReturn(moduleIdentifier);
		given(scriptResource.source()).willReturn("");
		
		mse = new ModuleScriptEnvironment(dependencies, requiredModule);
	}
	
	@Test
	public void testUserModuleScript() {
		construct("module", "module", Base);
		
		assertThat(mse.scope().get("inject", mse.scope()), is(ScriptableObject.NOT_FOUND));
	}

	@Test
	public void testApiModuleScript() {
		
		construct("jj/module", "module", APIModules);
		
		assertThat(mse.scope().get("inject", mse.scope()), is(instanceOf(Function.class)));
	}
}
