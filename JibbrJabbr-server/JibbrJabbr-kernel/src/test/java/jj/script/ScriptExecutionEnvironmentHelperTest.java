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
package jj.script;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.execution.MockJJExecutors;
import jj.execution.MockJJExecutors.ThreadType;
import jj.resource.ResourceFinder;
import jj.resource.document.ScriptResource;
import jj.resource.document.ScriptResourceType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptExecutionEnvironmentHelperTest {
	
	@Mock ResourceFinder finder;
	ScriptExecutionEnvironments scriptExecutionEnvironments;
	@Mock ScriptExecutionEnvironmentCreator creator;
	MockJJExecutors executors;
	@Mock ScriptResource scriptResource;
	@Mock ScriptResource scriptResource1;
	@Mock ScriptResource scriptResource2;
	@Mock ModuleScriptExecutionEnvironment moduleScriptExecutionEnvironment;
	@Mock DocumentScriptExecutionEnvironment associatedScriptExecutionEnvironment;
	
	@Before
	public void before() {
		scriptExecutionEnvironments = new ScriptExecutionEnvironments();
		executors = new MockJJExecutors();
	}

	@Test
	public void testFindModuleScriptExecutionEnvironment() {
		String baseName = "index";
		String moduleIdentifier = "helpers/linkify";
		
		given(finder.findResource(ScriptResource.class, ScriptResourceType.Module.suffix(moduleIdentifier))).willReturn(scriptResource);
		given(creator.createScriptExecutionEnvironment(scriptResource, moduleIdentifier, baseName)).willReturn(moduleScriptExecutionEnvironment);
		
		ScriptExecutionEnvironmentHelper underTest = new ScriptExecutionEnvironmentHelper(finder, scriptExecutionEnvironments, creator, executors);
		
		ModuleScriptExecutionEnvironment result = underTest.scriptExecutionEnvironmentFor(baseName, moduleIdentifier);
		
		assertThat(result, is(moduleScriptExecutionEnvironment));
	}

	@Test
	public void testFindModuleScriptExecutionEnvironment2() {
		String baseName = "chat/index";
		String moduleIdentifier = "helpers/linkify";
		
		given(finder.findResource(ScriptResource.class, ScriptResourceType.Module.suffix("chat/helpers/linkify"))).willReturn(scriptResource);
		given(creator.createScriptExecutionEnvironment(scriptResource, moduleIdentifier, baseName)).willReturn(moduleScriptExecutionEnvironment);
		
		ScriptExecutionEnvironmentHelper underTest = new ScriptExecutionEnvironmentHelper(finder, scriptExecutionEnvironments, creator, executors);
		
		ModuleScriptExecutionEnvironment result = underTest.scriptExecutionEnvironmentFor(baseName, moduleIdentifier);
		
		assertThat(result, is(moduleScriptExecutionEnvironment));
	}
	
	@Test
	public void testFindAssociatedScriptExecutionEnvironment() {
		String baseName = "index";
		
		given(finder.findResource(ScriptResource.class, ScriptResourceType.Client.suffix("index"))).willReturn(scriptResource1);
		given(finder.findResource(ScriptResource.class, ScriptResourceType.Shared.suffix("index"))).willReturn(null);
		given(finder.findResource(ScriptResource.class, ScriptResourceType.Server.suffix("index"))).willReturn(scriptResource2);
		
		given(creator.createScriptExecutionEnvironment(scriptResource1, null, scriptResource2, baseName)).willReturn(associatedScriptExecutionEnvironment);
		
		executors.addThreadTypes(ThreadType.ScriptThread, 10);
		
		ScriptExecutionEnvironmentHelper underTest = new ScriptExecutionEnvironmentHelper(finder, scriptExecutionEnvironments, creator, executors);
		
		DocumentScriptExecutionEnvironment result = underTest.scriptExecutionEnvironmentFor(baseName);
		
		assertThat(result, is(associatedScriptExecutionEnvironment));
	}
}
