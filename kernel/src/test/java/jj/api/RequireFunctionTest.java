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
package jj.api;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;
import jj.resource.ResourceFinder;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.RequiredModule;
import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class RequireFunctionTest {
	
	@Mock CurrentScriptEnvironment env;
	@Mock ResourceFinder resourceFinder;
	
	@Mock ScriptEnvironment rootScriptEnvironment;
	
	@InjectMocks RequireFunction requireFunction;
	
	@Mock Scriptable scope;
	@Captor ArgumentCaptor<String> nameCaptor;
	@Captor ArgumentCaptor<RequiredModule> requiredModuleCaptor;
	
	@Before
	public void before() {
		given(env.currentRootScriptEnvironment()).willReturn(rootScriptEnvironment);
	}

	private void givenAnExistingModule() {
		ModuleScriptEnvironment mse = mock(ModuleScriptEnvironment.class);
		given(mse.initialized()).willReturn(true);
		given(resourceFinder.findResource(eq(ModuleScriptEnvironment.class), anyString(), anyVararg())).willReturn(mse);
	}
	
	private void verifyIdentifierResolution(String expected) {
		verify(resourceFinder).findResource(eq(ModuleScriptEnvironment.class), nameCaptor.capture(), requiredModuleCaptor.capture());
		assertThat(nameCaptor.getValue(), is(expected));
		assertThat(requiredModuleCaptor.getValue(), is(notNullValue()));
		// should test that the RequiredModule is correct, will need a helper in that package though
	}
	
	@Test
	public void testIdentifierResolution1() {
		
		givenAnExistingModule();
		
		requireFunction.call(null, scope, null, new Object[] {"./helper", "index"});
		
		verifyIdentifierResolution("helper");
	}
	
	@Test
	public void testIdentifierResolution2() {
		
		givenAnExistingModule();
		
		requireFunction.call(null, scope, null, new Object[] {"helper", "index"});
		
		verifyIdentifierResolution("helper");
	}
	
	@Test
	public void testIdentifierResolution3() {
		
		givenAnExistingModule();
		
		requireFunction.call(null, scope, null, new Object[] {"helper2", "modules/helper"});
		
		verifyIdentifierResolution("modules/helper2");
	}
	
	@Test
	public void testIdentifierResolution4() {
		
		givenAnExistingModule();
		
		requireFunction.call(null, scope, null, new Object[] {"../modules2/helper", "modules/helper2"});
		
		verifyIdentifierResolution("modules2/helper");
	}
	
	@Test
	public void testFailedIdentifierResolution() {
		
		try {
			requireFunction.call(null, scope, null, new Object[] {"../helper", "index"});
			fail();
		} catch (APIException e) {
			// yay!
			// need a good message
		}
	}

	@Test
	public void testArguments() {
		
		boolean caught = false;
		try {
			requireFunction.call(null, scope, null, null);
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(true));
		
		caught = false;
		
		try {
			requireFunction.call(null, scope, null, new Object[] {""});
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(true));
		
		caught = false;
		
		try {
			requireFunction.call(null, scope, null, new Object[] {"", "", ""});
		} catch (AssertionError ae) {
			caught = true;
		}
		
		assertThat(caught, is(true));
	}

}
