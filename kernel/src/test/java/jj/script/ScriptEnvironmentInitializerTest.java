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
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import jj.event.Publisher;
import jj.execution.JJTask;
import jj.execution.MockTaskRunner;
import jj.execution.TaskHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Script;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ScriptEnvironmentInitializerTest {
	
	PendingKey pendingKey1;
	PendingKey pendingKey2;
	
	MockTaskRunner taskRunner;

	ScriptEnvironmentInitializer sei;
	
	@Mock AbstractScriptEnvironment scriptEnvironment;
	
	@Mock Script script;
	
	@Mock IsThread isScriptThread;
	
	@Mock Publisher publisher;
	@Captor ArgumentCaptor<ScriptEnvironmentInitialized> initEventCaptor;
	@Captor ArgumentCaptor<ScriptEnvironmentInitializationError> errorEventCaptor;
	
	@Before
	public void before() {
		pendingKey1 = new PendingKey();
		pendingKey2 = new PendingKey();
		taskRunner = new MockTaskRunner();
		given(scriptEnvironment.script()).willReturn(script);
		sei = new ScriptEnvironmentInitializer(taskRunner, isScriptThread, publisher);
	}
	
	@Test
	public void testRootScriptEnvironmentInitialization() throws Exception {
		ScriptTask<?> resumable = startInitialization();
		
		given(scriptEnvironment.beginInitializing()).willReturn(pendingKey1);
		
		taskRunner.runFirstTask();
		
		assertThat(ScriptTaskHelper.pendingKey(resumable), is(pendingKey1));
		verify(scriptEnvironment).beginInitializing();
		
		Object result = new Object();
		ScriptTaskHelper.resumeWith(resumable, result);
		
		given(scriptEnvironment.resumeContinuation(pendingKey1, result)).willReturn(pendingKey2);
		
		// put it back!
		taskRunner.tasks.add(resumable);
		taskRunner.runFirstTask();
		
		verify(scriptEnvironment, never()).initialized(true);
		assertThat(ScriptTaskHelper.pendingKey(resumable), is(pendingKey2));
		
		result = new Object();
		ScriptTaskHelper.resumeWith(resumable, result);
		
		taskRunner.tasks.add(resumable);
		taskRunner.runFirstTask();
		
		verify(scriptEnvironment).initialized(true);
		verify(publisher).publish(initEventCaptor.capture());
		assertThat(initEventCaptor.getValue().scriptEnvironment(), is((ScriptEnvironment)scriptEnvironment));
		assertThat(ScriptTaskHelper.pendingKey(resumable), is(nullValue()));
	}
	
	@Test
	public void testRootScriptEnvironmentInitializationErrored() throws Exception {
		ScriptTask<?> resumable = startInitialization();
		RuntimeException re = new RuntimeException();
		
		TaskHelper.errored(resumable, re);
		
		verify(scriptEnvironment).initializationError(re);
		verify(publisher).publish(errorEventCaptor.capture());
		assertThat(errorEventCaptor.getValue().scriptEnvironment(), is((ScriptEnvironment)scriptEnvironment));
		assertThat(errorEventCaptor.getValue().cause(), is((Throwable)re));
	}

	private ScriptTask<?> startInitialization() {
		// this just puts the task into our greedy little hands
		sei.initializeScript(scriptEnvironment);
		JJTask task = taskRunner.tasks.get(0);
		ScriptTask<?> resumable = (ScriptTask<?>)task;
		return resumable;
	}

}
