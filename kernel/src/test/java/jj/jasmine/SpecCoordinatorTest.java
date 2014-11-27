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
		
		sc = new SpecCoordinator(taskRunner, publisher);
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
		assertTrue(taskRunner.tasks.isEmpty());

		// should have the continuation coordinator execute in this order 
		InOrder io = inOrder(jse);
		io.verify(jse).executeScript(specScript);
		io.verify(jse).executeScript(targetScript);
		io.verify(jse).executeScript(runnerScript);
		
		// and nothing gets published
		assertTrue(publisher.events.isEmpty());
	}
	
	private void verifyErrorEvent(final String context) {
		
		assertThat(publisher.events.size(), is(1));
		assertThat(publisher.events.get(0), is(instanceOf(JasmineTestError.class)));
		
		JasmineTestError e = (JasmineTestError)publisher.events.get(0);
		e.describeTo(logger);
		
		verify(logger).error(JasmineTestError.MESSAGE_1, spec, context, target);
		verify(logger).error(JasmineTestError.MESSAGE_2, exception);
	}
	
	@Test
	public void testSpecExecutionErrors() throws Exception {
		
		// given
		given(jse.executeScript(specScript)).willThrow(exception);
		
		// when
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));
		
		// then
		taskRunner.runFirstTask();
		assertTrue(taskRunner.tasks.isEmpty());
		
		verify(jse).executeScript(specScript);
		
		verifyErrorEvent(SpecCoordinator.CONTEXT_SPEC);
	}
	
	@Test
	public void testTargetExecutionErrors() throws Exception {
		
		// given
		given(jse.executeScript(targetScript)).willThrow(exception);
		
		// when
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));
		
		// then
		taskRunner.runFirstTask();
		taskRunner.runFirstTask();
		assertTrue(taskRunner.tasks.isEmpty());
		
		InOrder io = inOrder(jse);
		io.verify(jse).executeScript(specScript);
		io.verify(jse).executeScript(targetScript);
		
		verifyErrorEvent(SpecCoordinator.CONTEXT_TARGET);
	}
	
	@Test
	public void testRunnerExecutionErrors() throws Exception {
		
		// given
		given(jse.executeScript(runnerScript)).willThrow(exception);
		
		// when
		sc.scriptInitialized(new ScriptEnvironmentInitialized(jse));
		
		// then
		taskRunner.runFirstTask();
		taskRunner.runFirstTask();
		taskRunner.runFirstTask();
		assertTrue(taskRunner.tasks.isEmpty());
		
		InOrder io = inOrder(jse);
		io.verify(jse).executeScript(specScript);
		io.verify(jse).executeScript(targetScript);
		io.verify(jse).executeScript(runnerScript);
		
		verifyErrorEvent(SpecCoordinator.CONTEXT_RUNNER);
	}

}
