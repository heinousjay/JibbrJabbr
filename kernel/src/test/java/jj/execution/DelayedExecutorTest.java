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

import static org.junit.Assert.*;
import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import jj.execution.DelayedExecutor.CancelKey;
import jj.util.MockClock;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class DelayedExecutorTest {
	
	/**
	 * 
	 */
	private static final int LATCH_WAIT_TIME = 200;

	MockClock clock;
	
	@Mock JJUncaughtExceptionHandler jjUncaughtExceptionHandler;

	DelayedExecutor de;
	
	public void makeMultiThreaded() {
		de = new DelayedExecutor(
			clock = new MockClock(),
			2,
			2,
			10000, MILLISECONDS,
			new SynchronousQueue<Runnable>(),
			new JJThreadFactory(jjUncaughtExceptionHandler).namePattern("%d"),
			new JJRejectedExecutionHandler()
		) {
			protected String schedulerThreadName() {
				return "";
			};
		};
	}
	
	public void makeSingleThreaded() {
		de = new DelayedExecutor(
			clock = new MockClock(),
			1,
			1,
			10000, MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(),
			new JJThreadFactory(jjUncaughtExceptionHandler).namePattern("%d"),
			new JJRejectedExecutionHandler()
		) {
			protected String schedulerThreadName() {
				return "";
			};
			
			@Override
			protected boolean asynchronousScheduling() {
				return false;
			}
		};
	}
	
	@After
	public void after() {
		de.shutdownNow();
	}
	
	private CountDownLatch latch;
	
	private Runnable helper = new Runnable() {
		
		@Override
		public void run() {
			latch.countDown();
		}
	};
	
	@Test
	public void testNoDelayExecutesRightAwayMultiThreaded() throws Exception {
		makeMultiThreaded();
		latch = new CountDownLatch(1);
		
		de.submit(helper, 0, MILLISECONDS);
		
		assertTrue(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
	}
	
	@Test
	public void testNoDelayExecutesRightAwaySingleThreaded() throws Exception {
		makeSingleThreaded();
		latch = new CountDownLatch(1);
		
		de.submit(helper, 0, MILLISECONDS);
		
		assertTrue(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
	}
	
	@Test
	public void testDelayExecutesWhenItSaysMultiThreaded() throws Exception {
		makeMultiThreaded();
		latch = new CountDownLatch(1);
		
		de.submit(helper, 1, MILLISECONDS);
		
		assertFalse(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
		
		clock.advance();
		
		assertTrue(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
	}
	
	@Test
	public void testDelayExecutesWhenItSaysSingleThreaded() throws Exception {
		makeSingleThreaded();
		latch = new CountDownLatch(1);
		
		de.submit(helper, 1, MILLISECONDS);
		
		assertFalse(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
		
		clock.advance();
		
		assertTrue(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
	}

	@Test
	public void testCancelStopsExecutionMultiThreaded() throws Exception {
		makeMultiThreaded();
		latch = new CountDownLatch(1);
		
		CancelKey cancelKey = de.submit(helper, 1, MILLISECONDS);
		
		cancelKey.cancel();
		
		clock.advance(1, MILLISECONDS);

		assertFalse(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
	}

	@Test
	public void testCancelStopsExecutionSingleThreaded() throws Exception {
		makeMultiThreaded();
		latch = new CountDownLatch(1);
		
		CancelKey cancelKey = de.submit(helper, 1, MILLISECONDS);
		
		cancelKey.cancel();
		
		clock.advance(1, MILLISECONDS);

		assertFalse(latch.await(LATCH_WAIT_TIME, MILLISECONDS));
	}
}
