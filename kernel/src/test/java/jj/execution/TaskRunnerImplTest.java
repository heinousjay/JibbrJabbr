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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jj.event.MockPublisher;
import jj.event.MockPublisher.OnPublish;
import jj.logging.Emergency;
import jj.script.ScriptEnvironment;
import jj.util.MockClock;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TaskRunnerImplTest {
	
	private Runnable monitorTask;
	
	private @Mock ScriptEnvironment<?> scriptEnvironment;

	private @Mock Executors executors;
	
	private CurrentTask currentTask;
	
	private MockPublisher publisher;
	
	private TaskRunnerImpl executor;
	
	private @Captor ArgumentCaptor<JJTask<?>> taskCaptor;
	
	private @Captor ArgumentCaptor<Runnable> runnableCaptor;
	
	private String baseName = "test";
	
	private MockClock clock = new MockClock();
	
	@Before
	public void before() {
		
		currentTask = new CurrentTask();
		
		executor = new TaskRunnerImpl(executors, currentTask, publisher = new MockPublisher(), clock);
		
		verify(executors).executeTask(taskCaptor.capture(), runnableCaptor.capture());
		
		monitorTask = runnableCaptor.getValue();
		reset(executors);
		
		given(scriptEnvironment.name()).willReturn(baseName);
	}
	
	private Runnable getRunnable() {
		verify(executors).executeTask(taskCaptor.capture(), runnableCaptor.capture());
		// reset after pulling a runnable so that a test can control execution
		// one task at a time
		reset(executors);
		return runnableCaptor.getValue();
	}
	
	private void runTask() {
		getRunnable().run();
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
	public void testInterruption() throws Throwable {
		
		final CountDownLatch latch1 = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(1);
		final AtomicBoolean completed = new AtomicBoolean();
		final AtomicBoolean interrupted = new AtomicBoolean();
		
		final ServerTask task = new ServerTask("interruption test task") {
			@Override
			protected void run() throws Exception {
				latch1.countDown();
				try {
					Thread.sleep(300);
					completed.set(true);
				} catch (InterruptedException ie) {
					interrupted.set(true);
					latch2.countDown();
					throw ie;
				}
			}
		};
		
		final ServerTask task2 = new ServerTask("promised and ignored test task") {
			@Override
			protected void run() throws Exception {
				// doesn't even matter, just shouldn't end up even trying
			}
		};
		
		executor.execute(task).then(task2);
		Thread runningThread = new Thread(getRunnable());
		runningThread.setDaemon(true);
		runningThread.start();
		
		assertTrue(latch1.await(500, MILLISECONDS));
		task.interrupt();
		assertTrue(latch2.await(500, MILLISECONDS));
		assertTrue(interrupted.get());
		assertFalse(completed.get());
		
		verifyZeroInteractions(executors);
	}
	
	@Ignore // this refuses to work consistently
	@Test
	public void testMonitor() throws Throwable {
		// given
		// okay, gotta start the monitorTask up to test it, right?
		final Thread t = new Thread(monitorTask, "test thread");
		t.setDaemon(true);
		t.start();
		
		final ServerTask task = new ServerTask("test task") {
			@Override
			protected void run() throws Exception {
				System.out.println("hello darkness my old friend");
			}
		};
		
		// still in the given, we're putting a task in the queue
		executor.execute(task);
		// and expiring it
		clock.advance(TaskTracker.MAX_QUEUED_TIME + 1, MILLISECONDS);
		
		// and we need to coordinate
		final CountDownLatch latch = new CountDownLatch(1);
		publisher.onPublish = new OnPublish() {
			
			@Override
			public void published(Object event) {
				System.out.println("totally got called " + event);
				latch.countDown();
			}
		};
		
		// now we need to trigger the monitor to wake up.  this requires yet another
		// thread.  this one is not a daemon on purpose
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("well at least i got called");
				// causes the queue to get polled and have the system realize something has expired
				// takes advantage of the implementation of DelayQueue, so not ideal, but i got nothing
				// else!
				executor.execute(task);
				reset(executors);
			}
		}); //.start();
		
		assertTrue("timed out", latch.await(2, SECONDS));
		
		assertThat(publisher.events.get(0), is(instanceOf(Emergency.class)));
	}
}
