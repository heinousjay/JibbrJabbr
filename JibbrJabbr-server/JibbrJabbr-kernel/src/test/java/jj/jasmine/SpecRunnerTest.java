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
import jj.execution.events.ExecutionEnvironmentInitialized;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.SpecResource;
import jj.script.ScriptExecutionEnvironment;

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
	
	@Mock ScriptExecutionEnvironment executionEnvironment;
	@Mock ScriptResource scriptResource;
	@Mock SpecResource specResource;
	
	@Mock ResourceFinder resourceFinder;
	
	@InjectMocks SpecRunner specRunner;

	@Test
	public void test() {
		
		given(executionEnvironment.scriptName()).willReturn("whatever.js");
		given(resourceFinder.findResource(ScriptResource.class, executionEnvironment.scriptName())).willReturn(scriptResource);
		given(resourceFinder.findResource(SpecResource.class, executionEnvironment.scriptName())).willReturn(specResource);
		
		specRunner.findAndExecuteSpec(new ExecutionEnvironmentInitialized(executionEnvironment));
		
		// need to be dependent upon each other so that changes to either cause
		// things to get reloaded
		verify(scriptResource).dependsOn(specResource);
		verify(specResource).dependsOn(scriptResource);
	}

}
