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

import static org.mockito.BDDMockito.*;
import jj.engine.RequiredModuleException;
import jj.execution.MockJJExecutors;
import jj.resource.ResourceFinder;
import jj.resource.document.ScriptResource;
import jj.resource.document.ScriptResourceType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RequiredModuleContinuationProcessorTest {
	
	String baseName = "index";
	
	String module = "module";
	
	@Mock CurrentScriptContext context;
	
	MockJJExecutors executors;
	
	@Mock ResourceFinder finder;
	
	@Mock ScriptExecutionEnvironmentFinder scriptFinder;
	
	@Mock ContinuationState continuationState;
	
	RequiredModule requiredModule;

	RequiredModuleContinuationProcessor processor;
	
	@Mock ScriptRunner scriptRunner;
	
	@Mock ScriptResource scriptResource;
	
	@Mock ModuleScriptExecutionEnvironment scriptExecutionEnvironment;
	
	@Before
	public void before() {
		
		executors = new MockJJExecutors();
		
		given(context.baseName()).willReturn(baseName);
		
		processor = new RequiredModuleContinuationProcessor(context, executors, scriptRunner, finder, scriptFinder);
		
		requiredModule = new RequiredModule(module, context);
		
		given(continuationState.requiredModule()).willReturn(requiredModule);
	}
	
	@Test
	public void testFirstRequireOfModule() throws Exception {
		
		// given
		given(finder.loadResource(ScriptResource.class, ScriptResourceType.Module.suffix(module))).willReturn(scriptResource);
		
		// when
		processor.process(continuationState);
		executors.runUntilIdle();
		
		// then
		verify(scriptRunner).submit(requiredModule);
	}
	
	@Test
	public void testFirstRequireOfModuleNotFoundError() throws Exception {
		
		// when
		processor.process(continuationState);
		executors.runUntilIdle();
		
		// then
		verify(scriptRunner).restartAfterContinuation(anyString(), any(RequiredModuleException.class));
	}
	
	@Test
	public void testSecondRequireOfLoadedModule() {
		
		// given
		Scriptable exports = mock(Scriptable.class);
		
		given(scriptExecutionEnvironment.exports()).willReturn(exports);
		given(finder.findResource(ScriptResource.class, ScriptResourceType.Module.suffix(module))).willReturn(scriptResource);
		given(scriptFinder.forBaseNameAndModuleIdentifier(baseName, module)).willReturn(scriptExecutionEnvironment);
		given(scriptExecutionEnvironment.sha1()).willReturn("");
		given(scriptResource.sha1()).willReturn("");
		
		// when
		processor.process(continuationState);
		
		// then
		verify(scriptRunner).restartAfterContinuation(anyString(), any(Scriptable.class));
	}
	
	@Test
	public void testRequireOfObseleteModule() {
		
		// given
		given(finder.findResource(ScriptResource.class, ScriptResourceType.Module.suffix(module))).willReturn(scriptResource);
		given(scriptFinder.forBaseNameAndModuleIdentifier(baseName, module)).willReturn(scriptExecutionEnvironment);
		given(scriptExecutionEnvironment.sha1()).willReturn("sha1");
		given(scriptResource.sha1()).willReturn("sha2");
		
		// when
		processor.process(continuationState);
		
		// then
		verify(scriptRunner).submit(requiredModule);
	}

}
