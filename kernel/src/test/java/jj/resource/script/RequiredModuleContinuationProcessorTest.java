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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import jj.execution.IOTask;
import jj.execution.MockJJExecutor;
import jj.resource.ResourceFinder;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.RequiredModule;
import jj.resource.script.RequiredModuleContinuationProcessor;
import jj.script.ContinuationPendingKey;
import jj.script.ContinuationState;
import jj.script.DependsOnScriptEnvironmentInitialization;

import org.junit.Before;
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
public class RequiredModuleContinuationProcessorTest {
	
	ContinuationPendingKey pendingKey;
	
	String baseName = "index";
	
	String module = "module";
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	MockJJExecutor executor;
	
	@Mock ResourceFinder finder;
	
	@Mock ContinuationState continuationState;
	
	@Mock DependsOnScriptEnvironmentInitialization scriptEnvironmentInitializer;
	
	RequiredModule requiredModule;

	RequiredModuleContinuationProcessor processor;
	
	@Mock ModuleScriptEnvironment moduleScriptEnvironment;
	
	@Before
	public void before() {
		
		pendingKey = new ContinuationPendingKey();
		
		executor = new MockJJExecutor();
		
		processor = new RequiredModuleContinuationProcessor(executor, finder, scriptEnvironmentInitializer);
		
		requiredModule = new RequiredModule(documentScriptEnvironment, module);
		requiredModule.pendingKey(pendingKey);
		
		given(continuationState.continuationAs(RequiredModule.class)).willReturn(requiredModule);
	}
	
	@Test
	public void testFirstRequireOfModule() throws Exception {
		
		// given
		given(finder.loadResource(ModuleScriptEnvironment.class, module, requiredModule)).willReturn(moduleScriptEnvironment);
		
		// when
		processor.process(continuationState);
		
		// then
		// prove that an IO task was submitted
		assertThat(executor.tasks.size(), is(1));
		assertThat(executor.tasks.get(0), is(instanceOf(IOTask.class)));
		
		// and run it
		executor.runUntilIdle();
		
		// then
		// we validate it happened because it's the only signal we get.  the parent is
		verify(finder).loadResource(ModuleScriptEnvironment.class, module, requiredModule);
	}
	
	@Test
	public void testFirstRequireOfModuleNotFoundError() throws Exception {
		
		// when
		processor.process(continuationState);
		executor.runUntilIdle();
		// this shit is broken!
		// then
		assertThat(executor.pendingKey, is(pendingKey));
		assertThat(executor.result, is(instanceOf(RequiredModuleException.class)));
	}
	
	private void givenAScriptEnvironment() {
		given(finder.findResource(eq(ModuleScriptEnvironment.class), eq(module), any(RequiredModule.class))).willReturn(moduleScriptEnvironment);
	}
	
	@Test
	public void testSecondRequireOfLoadedModule() {
		
		// given
		ScriptableObject exports = mock(ScriptableObject.class);
		
		given(moduleScriptEnvironment.exports()).willReturn(exports);
		given(moduleScriptEnvironment.initialized()).willReturn(true);
		givenAScriptEnvironment();
		
		// when
		processor.process(continuationState);
		
		assertThat(executor.pendingKey, is(pendingKey));
		assertThat(executor.result, is((Object)exports));
	}

}
