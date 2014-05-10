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
package jj.script.resource;

import static org.mockito.BDDMockito.*;
import jj.configuration.resolution.AppLocation;
import jj.resource.ResourceKey;
import jj.resource.ResourceInstanceCreator;
import jj.script.ScriptEnvironment;
import jj.script.resource.ModuleScriptEnvironment;
import jj.script.resource.ModuleScriptEnvironmentCreator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * this test only validates that the creator is getting all of the necessary stuff to the resource. there needs to
 * be a separate test for creating a resource to validate it works as expected
 * 
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ModuleScriptEnvironmentCreatorTest {

	private @Mock ResourceInstanceCreator creator;
	
	private ModuleScriptEnvironmentCreator toTest;
	
	private @Mock ScriptEnvironment environment;
	
	@Before
	public void before() {
		toTest = new ModuleScriptEnvironmentCreator(null, creator);
	}
	
	@Test
	public void test() throws Exception {
		String name = "name";
		RequiredModule requiredModule = new RequiredModule(environment, name);
		toTest.createScriptEnvironment(name, requiredModule);
		
		verify(creator).createResource(
			eq(ModuleScriptEnvironment.class),
			any(ResourceKey.class),
			eq(AppLocation.Virtual),
			eq(name),
			eq(requiredModule)
		);
	}
}
