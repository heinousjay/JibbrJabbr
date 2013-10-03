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
import jj.resource.ResourceFinder;
import jj.resource.document.ExecutionEnvironmentInitialized;
import jj.resource.document.ScriptEnvironment;
import jj.resource.document.ScriptResource;
import jj.resource.spec.SpecResource;

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
	
	@Mock ScriptEnvironment scriptEnvironment;
	@Mock ScriptResource scriptResource;
	@Mock SpecResource specResource;
	
	@Mock ResourceFinder resourceFinder;
	
	@InjectMocks SpecRunner specRunner;

	@Test
	public void test() {
		
		given(scriptEnvironment.scriptName()).willReturn("whatever.js");
		given(resourceFinder.findResource(ScriptResource.class, scriptEnvironment.scriptName())).willReturn(scriptResource);
		given(resourceFinder.findResource(SpecResource.class, scriptEnvironment.scriptName())).willReturn(specResource);
		
		specRunner.findAndExecuteSpec(new ExecutionEnvironmentInitialized(scriptEnvironment));
		
		// need to be dependent upon each other so that changes to either cause
		// things to get reloaded
		verify(scriptResource).addDependent(specResource);
		verify(specResource).addDependent(scriptResource);
	}

}
