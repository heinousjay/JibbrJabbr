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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import jj.script.ContinuationPendingKey;
import jj.script.ScriptEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class TaskRunnerTest {
	
	@Mock IOExecutor ioExecutor;
	private @Mock ScheduledExecutorService scriptExecutor;
	
	@Mock ScriptEnvironment scriptEnvironment;

	ExecutorBundle bundle;
	
	CurrentTask currentTask;
	
	@Mock Logger logger;
	
	TaskRunnerImpl executor;
	
	@Captor ArgumentCaptor<Runnable> runnableCaptor;
	
	String baseName = "test";
	
	@Before
	public void before() {
		
		currentTask = new CurrentTask();
		
		Map<Class<?>, Object> executors = new HashMap<>();
		executors.put(IOExecutor.class, ioExecutor);
		bundle = new ExecutorBundle(executors);
		
		executor = new TaskRunnerImpl(bundle, currentTask, logger);
		
		given(scriptEnvironment.baseName()).willReturn(baseName);
	}
	
	private void runTask(ExecutorService service) {
		verify(service, atLeastOnce()).submit(runnableCaptor.capture());
		runnableCaptor.getValue().run();
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
		
		executor.execute(new IOTask("test task") {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		runTask(ioExecutor);
		
		verify(logger).error(anyString(), eq(toThrow));
	}
	
	
	private int count = 0;
	private final ResumableTask resumableTask = new ResumableTask("") {
		

		@Override
		protected void run() throws Exception {
			pendingKey = new ContinuationPendingKey();
			if (count++ > 0) {
				pendingKey = null;
			}
		}
		
		@Override
		protected Future<?> addRunnableToExecutor(ExecutorFinder executors, Runnable runnable) {
			return executors.ofType(IOExecutor.class).submit(runnable);
		}
	}; 
	
	@Test
	public void testResumableTask() {
		
		executor.execute(resumableTask);
		
		runTask(ioExecutor);
		
		executor.resume(resumableTask.pendingKey(), "whatever");
		
		runTask(ioExecutor);
		
		assertThat("whatever", is(resumableTask.result));
		assertThat(resumableTask.pendingKey, is(nullValue()));
		assertThat(count, is(2));
		
	}
}
