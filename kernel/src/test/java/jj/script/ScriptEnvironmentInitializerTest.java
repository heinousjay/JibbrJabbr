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
import jj.execution.MockJJExecutor;
import jj.execution.ResumableTask;
import jj.execution.TaskHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ScriptEnvironmentInitializerTest {
	
	ContinuationPendingKey pendingKey1;
	ContinuationPendingKey pendingKey2;
	
	MockJJExecutor executor;
	@Mock ContinuationCoordinator continuationCoordinator;

	ScriptEnvironmentInitializer sei;
	
	@Mock AbstractScriptEnvironment scriptEnvironment;
	
	@Mock Publisher publisher;
	@Captor ArgumentCaptor<ScriptEnvironmentInitialized> eventCaptor;
	
	@Before
	public void before() {
		pendingKey1 = new ContinuationPendingKey();
		pendingKey2 = new ContinuationPendingKey();
		executor = new MockJJExecutor();
		executor.isScriptThread = true;
		sei = new ScriptEnvironmentInitializer(executor, continuationCoordinator, publisher);
	}
	
	@Test
	public void testRootScriptEnvironmentInitialization() throws Exception {
		// this just puts the task into our greedy little hands
		sei.initializeScript(scriptEnvironment);
		JJTask task = executor.tasks.get(0);
		ResumableTask resumable = (ResumableTask)task;
		
		given(continuationCoordinator.execute(scriptEnvironment)).willReturn(pendingKey1);
		
		executor.runFirstTask();
		
		assertThat(TaskHelper.pendingKey(resumable), is(pendingKey1));
		verify(scriptEnvironment).initializing(true);
		
		Object result = new Object();
		TaskHelper.resumeWith(resumable, result);
		
		given(continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey1, result)).willReturn(pendingKey2);
		
		// put it back!
		executor.tasks.add(task);
		executor.runFirstTask();
		
		verify(scriptEnvironment, never()).initialized(true);
		assertThat(TaskHelper.pendingKey(resumable), is(pendingKey2));
		
		result = new Object();
		TaskHelper.resumeWith(resumable, result);
		
		executor.tasks.add(task);
		executor.runFirstTask();
		
		verify(continuationCoordinator).resumeContinuation(scriptEnvironment, pendingKey2, result);
		verify(scriptEnvironment).initialized(true);
		verify(publisher).publish(eventCaptor.capture());
		assertThat(eventCaptor.getValue().scriptEnvironment(), is((ScriptEnvironment)scriptEnvironment));
		assertThat(TaskHelper.pendingKey(resumable), is(nullValue()));
		
	}

}
