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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jj.event.MockPublisher;
import jj.execution.DelayedExecutor.CancelKey;
import jj.logging.Emergency;
import jj.script.ScriptEnvironment;
import jj.util.MockClock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TaskRunnerImplTest {
	
	private @Mock ServerExecutor serverExecutor;
	
	private Runnable monitorTask;
	
	@Mock CancelKey cancelKey;
	
	private @Mock ScriptEnvironment scriptEnvironment;

	private ExecutorBundle bundle;
	
	private CurrentTask currentTask;
	
	private MockPublisher publisher;
	
	private TaskRunnerImpl executor;
	
	private @Captor ArgumentCaptor<Runnable> runnableCaptor;
	
	private String baseName = "test";
	
	private MockClock clock = new MockClock();
	
	@Before
	public void before() {
		
		currentTask = new CurrentTask();
		
		Map<Class<?>, Object> executors = new HashMap<>();
		executors.put(ServerExecutor.class, serverExecutor);
		bundle = new ExecutorBundle(executors);
		
		executor = new TaskRunnerImpl(bundle, currentTask, publisher = new MockPublisher(), clock);
		
		verify(serverExecutor).submit(runnableCaptor.capture(), eq(0L), eq(MILLISECONDS));
		monitorTask = runnableCaptor.getValue();
		reset(serverExecutor);
		given(serverExecutor.submit(any(Runnable.class), any(Long.class), any(TimeUnit.class))).willReturn(cancelKey);
		
		given(scriptEnvironment.name()).willReturn(baseName);
	}
	
	private void runTask() {
		verify(serverExecutor).submit(runnableCaptor.capture(), eq(0L), eq(MILLISECONDS));
		// reset before each task run so that a test can control execution
		// one task at a time
		reset(serverExecutor);
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
		runTask();
		runTask();
		runTask();
		
		assertThat(counter.get(), is(3));
	}
	
	@Test
	public void testExecuteTask() {
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		ServerTask task = new ServerTask("test task") {
			@Override
			protected void run() throws Exception {
				flag.set(currentTask.current() == this);
			}
		};

		assertThat(currentTask.current(), is(nullValue()));
		
		executor.execute(task);
		runTask();
		
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
		
		runTask();
		
		assertThat(publisher.events.get(0), is(instanceOf(Emergency.class)));
	}
	
	@Test
	public void testMonitor() throws Throwable {
		// okay, gotta start the runnable
		final Thread t = new Thread(monitorTask, "test thread");
		t.setDaemon(true);
		t.start();
		
		ServerTask task = new ServerTask("test task") {
			@Override
			protected void run() throws Exception {
				t.setName("NO NO NO NO NO NO NO ");
			}
		};
		
		executor.execute(task);
		
		clock.advance(TaskTracker.MAX_QUEUED_TIME, MILLISECONDS);

		executor.execute(task);
		reset(serverExecutor);
		
		Thread.sleep(40);
		
		assertThat(publisher.events.get(0), is(instanceOf(Emergency.class)));
	}
}
