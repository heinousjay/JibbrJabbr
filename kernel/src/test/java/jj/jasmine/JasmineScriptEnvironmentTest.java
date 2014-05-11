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
package jj.jasmine;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.configuration.resolution.AppLocation.APIModules;
import jj.resource.ResourceFinder;
import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.module.ScriptResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JasmineScriptEnvironmentTest {

	@Mock ResourceFinder resourceFinder;
	@Mock ScriptableObject global;
	@Mock ScriptResource jasmineBoot;
	MockAbstractScriptEnvironmentDependencies dependencies;
	
	@Test
	public void test() {
		dependencies = new MockAbstractScriptEnvironmentDependencies();
		given(dependencies.rhinoContextProvider().context.newObject(global)).willReturn(global);
		given(resourceFinder.loadResource(eq(ScriptResource.class), eq(APIModules), eq("jasmine-boot.js"))).willReturn(jasmineBoot);
		
		JasmineScriptEnvironment jse = new JasmineScriptEnvironment(dependencies, global, resourceFinder);
		
		
	}

}
