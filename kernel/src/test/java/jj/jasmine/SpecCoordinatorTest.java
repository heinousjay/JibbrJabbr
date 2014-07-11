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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import jj.event.MockPublisher;
import jj.execution.MockTaskRunner;
import jj.script.ContinuationCoordinator;
import jj.script.ScriptEnvironmentInitialized;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Script;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecCoordinatorTest {

	@Mock ContinuationCoordinator continuationCoordinator;
	MockTaskRunner taskRunner;
	MockPublisher publisher;
	
	SpecCoordinator sc;
	
	@Mock JasmineScriptEnvironment jse;
	
	@Mock Script specScript;
	@Mock Script targetScript;
	@Mock Script runnerScript;
	
	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		publisher = new MockPublisher();
		
		given(jse.specScript()).willReturn(specScript);
		given(jse.targetScript()).willReturn(targetScript);
		given(jse.runnerScript()).willReturn(runnerScript);
		
		sc = new SpecCoordinator(continuationCoordinator, taskRunner, publisher);
	}
	
	@Test
	public void testHappyPath() throws Exception {
		
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));
		
		taskRunner.runFirstTask();
		
		verify(continuationCoordinator).execute(jse, specScript);
		
		taskRunner.runFirstTask();
		
		verify(continuationCoordinator).execute(jse, targetScript);
		
		taskRunner.runFirstTask();

		verify(continuationCoordinator).execute(jse, runnerScript);
		
		// essentially if we got here with no errors, things worked
		
		// need to understand error scenarios!
		
		assertThat(publisher.events.get(0), is(instanceOf(JasmineSpecExecutionCompleted.class)));
	}

}
