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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.*;

import jj.event.MockPublisher;
import jj.execution.MockTaskRunner;
import jj.script.ContinuationCoordinator;
import jj.script.ScriptEnvironmentInitialized;
import jj.script.module.ScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Script;
import org.slf4j.Logger;

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
	
	@Mock ScriptResource spec;
	@Mock ScriptResource target;
	
	@Mock Script specScript;
	@Mock Script targetScript;
	@Mock Script runnerScript;
	
	RuntimeException exception = new RuntimeException();
	
	@Mock Logger logger;
	
	// given
	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		publisher = new MockPublisher();
		
		given(jse.spec()).willReturn(spec);
		given(jse.target()).willReturn(target);
		given(jse.specScript()).willReturn(specScript);
		given(jse.targetScript()).willReturn(targetScript);
		given(jse.runnerScript()).willReturn(runnerScript);
		
		sc = new SpecCoordinator(continuationCoordinator, taskRunner, publisher);
	}
	
	@Test
	public void testHappyPath() throws Exception {
		
		// when
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));

		// then
		
		// should be three tasks to run one after the other
		taskRunner.runFirstTask();
		
		taskRunner.runFirstTask();
		
		taskRunner.runFirstTask();

		// should have the continuation coordinator execute in this order 
		InOrder io = inOrder(continuationCoordinator);
		io.verify(continuationCoordinator).execute(jse, specScript);
		io.verify(continuationCoordinator).execute(jse, targetScript);
		io.verify(continuationCoordinator).execute(jse, runnerScript);
		
		// and nothing gets published
		assertTrue(publisher.events.isEmpty());
	}
	
	private void verifyErrorEvent() {
		
		assertThat(publisher.events.size(), is(1));
		assertThat(publisher.events.get(0), is(instanceOf(JasmineTestError.class)));
		
		JasmineTestError e = (JasmineTestError)publisher.events.get(0);
		e.describeTo(logger);
		
		verify(logger).error("Jasmine spec error!\nrunning {} errored\ntargeting {}", spec, target);
		verify(logger).error("", exception);
	}
	
	@Test
	public void testSpecExecutionErrors() throws Exception {
		
		// given
		given(continuationCoordinator.execute(jse, specScript)).willThrow(exception);
		
		// when
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));
		
		// then
		taskRunner.runFirstTask();
		
		verify(continuationCoordinator).execute(jse, specScript);
		verifyNoMoreInteractions(continuationCoordinator);
		
		verifyErrorEvent();
	}
	
	@Test
	public void testTargetExecutionErrors() throws Exception {
		
		// given
		given(continuationCoordinator.execute(jse, targetScript)).willThrow(exception);
		
		// when
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));
		
		// then
		taskRunner.runFirstTask();
		taskRunner.runFirstTask();
		
		InOrder io = inOrder(continuationCoordinator);
		io.verify(continuationCoordinator).execute(jse, specScript);
		io.verify(continuationCoordinator).execute(jse, targetScript);
		verifyNoMoreInteractions(continuationCoordinator);
		
		verifyErrorEvent();
	}
	
	@Test
	public void testRunnerExecutionErrors() throws Exception {
		
		// given
		given(continuationCoordinator.execute(jse, runnerScript)).willThrow(exception);
		
		// when
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));
		
		// then
		taskRunner.runFirstTask();
		taskRunner.runFirstTask();
		taskRunner.runFirstTask();
		
		InOrder io = inOrder(continuationCoordinator);
		io.verify(continuationCoordinator).execute(jse, specScript);
		io.verify(continuationCoordinator).execute(jse, targetScript);
		io.verify(continuationCoordinator).execute(jse, runnerScript);
		
		verifyErrorEvent();
	}

}
