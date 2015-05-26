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

import static org.mockito.BDDMockito.*;
import static jj.jasmine.JasmineScriptEnvironment.*;
import static jj.system.ServerLocation.Virtual;
import static jj.resource.ResourceEventMaker.makeResourceLoaded;
import jj.resource.ResourceLoaded;
import jj.resource.ResourceLoader;
import jj.script.module.ScriptResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecRunnerTest {
	
	@Mock JasmineConfiguration configuration;
	@Mock JasmineSwitch jasmineSwitch;
	@Mock ResourceLoader resourceLoader;
	
	@InjectMocks SpecRunner specRunner;
	
	@Mock ScriptResource sr;

	@Test
	public void testNoAutorun() {
		ResourceLoaded rl = makeResourceLoaded(sr);
		specRunner.resourceLoaded(rl);
		
		verify(resourceLoader, never()).loadResource(eq(JasmineScriptEnvironment.class), eq(Virtual), anyString(), anyVararg());
	}
	
	@Test
	public void testAutorun() {
		
		given(configuration.autorunSpecs()).willReturn(true);
		given(sr.name()).willReturn("name.js");

		ResourceLoaded rl = makeResourceLoaded(sr);
		specRunner.resourceLoaded(rl);
		
		verify(resourceLoader).loadResource(JasmineScriptEnvironment.class, Virtual, "name-spec.js", rl);
	}
	
	@Test
	public void testIgnored() {

		given(configuration.autorunSpecs()).willReturn(true);
		
		given(sr.name()).willReturn(JASMINE_JS);
		ResourceLoaded rl = makeResourceLoaded(sr);
		specRunner.resourceLoaded(rl);

		given(sr.name()).willReturn(JASMINE_BOOT_JS);
		rl = makeResourceLoaded(sr);
		specRunner.resourceLoaded(rl);

		given(sr.name()).willReturn(JASMINE_RUN_JS);
		rl = makeResourceLoaded(sr);
		specRunner.resourceLoaded(rl);

		given(sr.name()).willReturn("name-spec.js");
		rl = makeResourceLoaded(sr);
		specRunner.resourceLoaded(rl);
		
		verifyZeroInteractions(resourceLoader);
	}

}
