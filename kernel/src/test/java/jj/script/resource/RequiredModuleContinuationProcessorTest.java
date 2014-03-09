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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import jj.configuration.AppLocation;
import jj.document.DocumentScriptEnvironment;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoaded;
import jj.resource.ResourceLoader;
import jj.resource.ResourceNotFound;
import jj.script.ContinuationPendingKey;
import jj.script.ContinuationPendingKeyResultExtractor;
import jj.script.ContinuationState;
import jj.script.DependsOnScriptEnvironmentInitialization;
import jj.script.resource.ModuleScriptEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
	
	@Mock ResourceLoader loader;
	
	@Mock ResourceFinder finder;
	
	@Mock ContinuationState continuationState;
	
	@Mock DependsOnScriptEnvironmentInitialization scriptEnvironmentInitializer;
	
	RequiredModule requiredModule;

	@InjectMocks RequiredModuleContinuationProcessor processor;
	
	@Mock ModuleScriptEnvironment moduleScriptEnvironment;
	
	@Before
	public void before() {
		
		pendingKey = new ContinuationPendingKey();
		
		requiredModule = new RequiredModule(documentScriptEnvironment, module);
		requiredModule.pendingKey(pendingKey);
		
		given(continuationState.continuationAs(RequiredModule.class)).willReturn(requiredModule);
	}
	
	public void performFirstRequireOfModule() throws Exception {
		
		// when
		processor.process(continuationState);
		
		// we validate it happened because it's the only signal we get
		verify(finder).findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, module, requiredModule);
		verify(loader).loadResource(ModuleScriptEnvironment.class, AppLocation.Virtual, module, requiredModule);
	}
	
	@Test
	public void testFirstRequiredOfModuleFound() throws Exception {

		// given
		performFirstRequireOfModule();
		
		// when
		processor.resourceLoaded(new ResourceLoaded(ModuleScriptEnvironment.class, AppLocation.Virtual, module, requiredModule));
		
		Object result = ContinuationPendingKeyResultExtractor.RESULT_MAP.remove(pendingKey);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testFirstRequireOfModuleNotFound() throws Exception {
		
		// given
		performFirstRequireOfModule();
		
		// when
		processor.resourceNotFound(new ResourceNotFound(ModuleScriptEnvironment.class, AppLocation.Virtual, module, requiredModule));
		
		// then
		Object result = ContinuationPendingKeyResultExtractor.RESULT_MAP.remove(pendingKey);
		
		assertThat(result, is(notNullValue()));
		assertThat(result, is(instanceOf(RequiredModuleException.class)));
	}
	
	private void givenAScriptEnvironment() {
		given(finder.findResource(eq(ModuleScriptEnvironment.class), eq(AppLocation.Virtual), eq(module), any(RequiredModule.class))).willReturn(moduleScriptEnvironment);
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
		
		Object result = ContinuationPendingKeyResultExtractor.RESULT_MAP.remove(pendingKey);
		assertThat(result, is((Object)exports));
	}

}
