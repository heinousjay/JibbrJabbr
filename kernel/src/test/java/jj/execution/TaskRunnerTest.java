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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jj.script.ScriptEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class TaskRunnerTest {
	
	private @Mock ServerExecutor serverExecutor;
	
	private @Mock ScriptEnvironment scriptEnvironment;

	private ExecutorBundle bundle;
	
	private CurrentTask currentTask;
	
	private @Mock Logger logger;
	
	private TaskRunnerImpl executor;
	
	private @Captor ArgumentCaptor<Runnable> runnableCaptor;
	
	private String baseName = "test";
	
	@Before
	public void before() {
		
		currentTask = new CurrentTask();
		
		Map<Class<?>, Object> executors = new HashMap<>();
		executors.put(ServerExecutor.class, serverExecutor);
		bundle = new ExecutorBundle(executors);
		
		executor = new TaskRunnerImpl(bundle, currentTask, logger);
		
		given(scriptEnvironment.name()).willReturn(baseName);
	}
	
	private void runTask(ServerExecutor service) {
		verify(service, atLeastOnce()).submit(runnableCaptor.capture(), eq(0L), eq(MILLISECONDS));
		// reset before each task run so that a test can control execution
		// one task at a time
		reset(service);
		runnableCaptor.getValue().run();
	}
	
	@Test
	public void testPromiseKeeping() {
		
		final AtomicInteger counter = new AtomicInteger();
		
		ServerTask task1 = new ServerTask("test task 1") {
			@Override
			protected void run() throws Exception {
				counter.incrementAndGet();
			}
		};
		
		ServerTask task2 = new ServerTask("test task 2") {
			@Override
			protected void run() throws Exception {
				counter.incrementAndGet();
			}
		};
		
		ServerTask task3 = new ServerTask("test task 3") {
			@Override
			protected void run() throws Exception {
				counter.incrementAndGet();
			}
		};
		
		executor.execute(task1).then(task2).then(task3);
		runTask(serverExecutor);
		runTask(serverExecutor);
		runTask(serverExecutor);
		
		assertThat(counter.get(), is(3));
	}
	
	@Test
	public void testExecuteIOTask() {
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		ServerTask task = new ServerTask("test task") {
			@Override
			protected void run() throws Exception {
				flag.set(currentTask.current() == this);
			}
		};

		assertThat(currentTask.current(), is(nullValue()));
		
		executor.execute(task);
		runTask(serverExecutor);
		
		assertThat(flag.get(), is(true));
		assertThat(currentTask.current(), is(nullValue()));
	}
	
	@Test
	public void testExecuteErrorLogged() {
		
		final Exception toThrow = new Exception();
		
		executor.execute(new ServerTask("test task") {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		runTask(serverExecutor);
		
		verify(logger).error(anyString(), eq(toThrow));
	}
}
