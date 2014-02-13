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
package jj.execution;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import jj.script.ContinuationPendingKey;
import jj.script.ScriptEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class JJExecutorImplTest {
	
	@Mock IOExecutor ioExecutor;
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	@Mock ScheduledExecutorService scriptExecutor;
	
	@Mock ScriptEnvironment scriptEnvironment;

	@InjectMocks ExecutorBundle bundle;
	
	CurrentTask currentTask;
	
	@Mock Logger logger;
	
	TaskRunnerImpl executor;
	
	@Captor ArgumentCaptor<Runnable> runnableCaptor;
	
	String baseName = "test";
	
	@Before
	public void before() {
		
		currentTask = new CurrentTask();
		
		executor = new TaskRunnerImpl(bundle, currentTask, logger);
		
		given(scriptEnvironment.baseName()).willReturn(baseName);
		given(scriptExecutorFactory.executorFor(scriptEnvironment)).willReturn(scriptExecutor);
	}
	
	private void runTask(ExecutorService service) {
		verify(service, atLeastOnce()).submit(runnableCaptor.capture());
		runnableCaptor.getValue().run();
	}
	
	@Test
	public void testExecuteScriptTask() {
		
		// kind og a 
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		ScriptTask<ScriptEnvironment> task = new ScriptTask<ScriptEnvironment>("test task", scriptEnvironment) {
			
			@Override
			protected void run() {
				flag.set(currentTask.current() == this);
			}
		};

		assertThat(currentTask.current(), is(nullValue()));
		
		executor.execute(task);
		runTask(scriptExecutor);
		
		assertThat(flag.get(), is(true));
		assertThat(currentTask.current(), is(nullValue()));
	}
	
	@Test
	public void testExecuteIOTask() {
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		IOTask task = new IOTask("test task") {
			@Override
			protected void run() throws Exception {
				flag.set(currentTask.current() == this);
			}
		};

		assertThat(currentTask.current(), is(nullValue()));
		
		executor.execute(task);
		runTask(ioExecutor);
		
		assertThat(flag.get(), is(true));
		assertThat(currentTask.current(), is(nullValue()));
	}
	
	@Test
	public void testExecuteErrorLogged() {
		
		final Exception toThrow = new Exception();
		
		executor.execute(new ScriptTask<ScriptEnvironment>("test task", scriptEnvironment) {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		executor.execute(new IOTask("test task") {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		runTask(scriptExecutor);
		
		runTask(ioExecutor);
		
		verify(logger, times(2)).error(anyString(), eq(toThrow));
	}
	
	private final class ResumableScriptTask extends ScriptTask<ScriptEnvironment> {

		int count = 0;
		
		ResumableScriptTask(ScriptEnvironment scriptEnvironment) {
			super("who needs a name", scriptEnvironment);
		}

		@Override
		protected void run() throws Exception {
			pendingKey = new ContinuationPendingKey();
			if (count++ > 0) {
				pendingKey = null;
			}
		}
	}
	
	@Test
	public void testResumableTask() {
		
		ResumableScriptTask task = new ResumableScriptTask(scriptEnvironment);
		
		executor.execute(task);
		
		runTask(scriptExecutor);
		
		executor.resume(task.pendingKey(), "whatever");
		
		runTask(scriptExecutor);
		
		assertThat("whatever", is(task.result));
		assertThat(task.pendingKey, is(nullValue()));
		assertThat(task.count, is(2));
		
	}
}
