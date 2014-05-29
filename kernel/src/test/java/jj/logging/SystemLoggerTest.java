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
package jj.logging;

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import jj.execution.MockTaskRunner;
import jj.util.RandomHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemLoggerTest {

	MockTaskRunner taskRunner;
	@Mock Loggers loggers;
	
	SystemLogger sl;
	Thread publishLoop;
	
	@Before
	public void before() throws Exception {
		taskRunner = new MockTaskRunner();
		
		sl = new SystemLogger(taskRunner, loggers);
		
		sl.start();
		
		publishLoop = taskRunner.runFirstTaskInDaemon();
	}
	
	@After
	public void after() {
		publishLoop.interrupt();
	}
	
	CountDownLatch latch;
	String threadName; 
	
	class HelperEvent1 extends LoggedEvent {

		@Override
		public void describeTo(Logger logger) {
			SystemLoggerTest.this.threadName = MDC.get(SystemLogger.THREAD_NAME);
			latch.countDown();
		}
		
	}
	
	@Test
	public void test() throws Exception {
		latch = new CountDownLatch(1);
		String name = "name 1";
		Thread.currentThread().setName(name);
		sl.log(new HelperEvent1());
		assertTrue(latch.await(400, MILLISECONDS));
		assertThat(threadName, is(name));
	}
	
	class HelperEvent2 extends LoggedEvent {

		@Override
		public void describeTo(Logger logger) {
			count.getAndIncrement();
		}
	}

	@Test
	public void testUnderLoad() throws Exception {
		// warm up runs
		loadTest(4, 7000, 7001, 3, 15);
		loadTest(4, 7000, 7001, 3, 15);
		loadTest(4, 7000, 7001, 3, 15);
		loadTest(4, 7000, 7001, 3, 15);
		
		
//		System.gc();
//		Thread.sleep(150);
		
		
//		for (int i = 0; i < 5; ++i) {
//			loadTest(4, 10000, 10001, 3, 15);
//			loadTest(4, 10000, 10001, 3, 15);
//			loadTest(4, 10000, 10001, 3, 15);
//			loadTest(4, 10000, 10001, 3, 15);
//			loadTest(4, 10000, 10001, 3, 15);
//			System.gc();
//			Thread.sleep(150);
//		}
		
		
//		loadTest(18, 250000, 250001, 30, 100);
	}
	
	/*
loadTest(4, 7000, 7001, 3, 15);
loadTest(4, 7000, 7001, 3, 15);
loadTest(4, 7000, 7001, 3, 15);
loadTest(4, 7000, 7001, 3, 15);
loadTest(18, 250000, 250001, 30, 100);

run 1
++++++++++++++++++++++++++++++++++++
28000, 572, 637
118342936 in use

run 2
+++++++++++
28000, 175, 177
182894056 in use

run 3
++++++
28000, 96, 99
258203504 in use

run 4
+++++++
28000, 112, 114
322754616 in use

run 5
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
4500000, 19565, 19909
2439246920 in use


	 */
	int run = 0;
	final AtomicInteger count = new AtomicInteger();
	final AtomicInteger expected = new AtomicInteger();
	
	private void loadTest(final int threadTotal, final int lowerBound, final int upperBound, final int timeout, final int waitTime) throws Exception {
		System.out.println("run " + ++run);
		ExecutorService executor = null;
		final HelperEvent2 helperEvent = new HelperEvent2();
		long start = System.nanoTime();
		long latchTime = 0;
		try {
			count.set(0);
			expected.set(0);
			latch = new CountDownLatch(threadTotal);
			executor = Executors.newFixedThreadPool(threadTotal);
			for (int i = 0; i < threadTotal; ++i) {
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						int runs = RandomHelper.nextInt(lowerBound, upperBound);
						expected.getAndAdd(runs);
						for (int i = 0; i < runs; ++i) {
							sl.log(helperEvent);
						}
						latch.countDown();
					}
				});
			}
			
			assertTrue("timed out", latch.await(timeout, SECONDS));
			latchTime = System.nanoTime();
			int lastCount;
			
			while ((lastCount = count.get()) != expected.get()) {
				Thread.sleep(waitTime);
				if(count.get() > lastCount) {
					System.out.print("+");
				} else {
					throw new AssertionError("count froze at " + count + ", waiting for " + expected);
				}
			}
			
			
		} finally {
			long now = System.nanoTime();
			System.out.println();
			System.out.println(
				"count = " +
				count + ", time to populate = " + 
				
				MILLISECONDS.convert(latchTime - start, NANOSECONDS) + ", time to finish processing = " +
				MILLISECONDS.convert(now - latchTime, NANOSECONDS)
			);
			String memory = DecimalFormat.getIntegerInstance().format(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
			System.out.println(memory + " in use");
			System.out.println();
			executor.shutdownNow();
		}
	}
}
