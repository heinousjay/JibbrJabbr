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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import jj.engine.RequiredModuleException;
import jj.execution.IOTask;
import jj.execution.MockJJExecutor;
import jj.resource.ResourceFinder;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.document.ModuleParent;
import jj.resource.document.ModuleScriptEnvironment;
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
	
	String baseName = "index";
	
	String module = "module";
	
	@Mock CurrentScriptContext context;
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	MockJJExecutor executors;
	
	@Mock ResourceFinder finder;
	
	@Mock ContinuationState continuationState;
	
	RequiredModule requiredModule;

	RequiredModuleContinuationProcessor processor;
	
	@Mock ScriptRunnerInternal scriptRunner;
	
	@Mock ModuleScriptEnvironment scriptEnvironment;
	
	@Before
	public void before() {
		
		executors = new MockJJExecutor();
		
		given(context.baseName()).willReturn(baseName);
		given(context.documentScriptEnvironment()).willReturn(documentScriptEnvironment);
		
		processor = new RequiredModuleContinuationProcessor(context, executors, scriptRunner, finder);
		
		requiredModule = new RequiredModule(module, context);
		
		given(continuationState.requiredModule()).willReturn(requiredModule);
	}
	
	@Test
	public void testFirstRequireOfModule() throws Exception {
		
		// given
		given(finder.loadResource(eq(ModuleScriptEnvironment.class), eq(module), any(ModuleParent.class))).willReturn(scriptEnvironment);
		
		// when
		processor.process(continuationState);
		
		// then
		// prove that an IO task was submitted
		assertThat(executors.tasks.size(), is(1));
		assertThat(executors.tasks.get(0), is(instanceOf(IOTask.class)));
		
		// and run it
		executors.runUntilIdle();
		
		// then
		verify(scriptRunner).submit(requiredModule, scriptEnvironment);
	}
	
	@Test
	public void testFirstRequireOfModuleNotFoundError() throws Exception {
		
		// when
		processor.process(continuationState);
		executors.runUntilIdle();
		
		// then
		verify(scriptRunner).submit(anyString(), any(ScriptContext.class), anyString(), any(RequiredModuleException.class));
	}
	
	private void givenAScriptEnvironment() {
		given(finder.findResource(eq(ModuleScriptEnvironment.class), eq(module), any(ModuleParent.class))).willReturn(scriptEnvironment);
	}
	
	@Test
	public void testSecondRequireOfLoadedModule() {
		
		// given
		ScriptableObject exports = mock(ScriptableObject.class);
		
		given(scriptEnvironment.exports()).willReturn(exports);
		given(scriptEnvironment.initialized()).willReturn(true);
		givenAScriptEnvironment();
		
		// when
		processor.process(continuationState);
		
		// then
		verify(scriptRunner).submit(anyString(), any(ScriptContext.class), anyString(), eq(exports));
	}
	
	@Test
	public void testRequireOfObseleteModule() {
		
		// given
		givenAScriptEnvironment();
		
		// when
		processor.process(continuationState);
		
		// then
		verify(scriptRunner).submit(requiredModule, scriptEnvironment);
	}

}
