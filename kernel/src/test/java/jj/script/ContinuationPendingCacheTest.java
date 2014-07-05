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
import static org.junit.Assume.assumeTrue;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jj.execution.TaskRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ContinuationPendingCacheTest {
	
	// just trying to make sure that the concurrency is done correctly

	private static final int STRESS_LEVEL = Runtime.getRuntime().availableProcessors(); // total number of threads to use for the test
	private static final int STRIDE = 400;
	private static final int KEY_COUNT = STRESS_LEVEL * STRIDE;
	
	private static final class HelperTask extends ScriptTask<ScriptEnvironment> {

		/** it will never run so nulls are cool!
		 */
		protected HelperTask() {
			super("", null, null);
		}

		@Override
		protected void begin() throws Exception {
			// never gonna run
		}
		
		
		protected void pendingKey(ContinuationPendingKey pendingKey) {
			this.pendingKey = pendingKey;
		}
		
		protected Object result() {
			return result;
		}
	}

	private @Mock TaskRunner taskRunner;
	private @InjectMocks ContinuationPendingCache cache;

	private HelperTask[] tasks;
	
	private ContinuationPendingKey[] pendingKeys;
	
	@Before
	public void before() {
		//System.out.println(KEY_COUNT + " keys on " + STRESS_LEVEL + " threads");
		assertThat(KEY_COUNT % STRESS_LEVEL, is(0));
		
		tasks = new HelperTask[KEY_COUNT];
		pendingKeys = new ContinuationPendingKey[KEY_COUNT];
		
		for (int i = 0; i < KEY_COUNT; ++i) {
			tasks[i] = new HelperTask();
			pendingKeys[i] = new ContinuationPendingKey(cache);
			tasks[i].pendingKey(pendingKeys[i]);
		}
	}
	
	@Test
	public void testStoreAndResume() throws Throwable {
		
		final ArrayBlockingQueue<Throwable> throwables = new ArrayBlockingQueue<>(STRESS_LEVEL);
		final CountDownLatch latch = new CountDownLatch(STRESS_LEVEL);
		final int stride = STRIDE; // 200 / 10 = 20;
		
		for (int i = 0; i < STRESS_LEVEL; ++i) {
			final int start = i * stride; // 0 * 20 = 0, 1 * 20 = 20;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						for (int i = start; i < start + stride; ++i) {
							cache.storeForContinuation(tasks[i]);
							Thread.yield(); // get em all good and mixed up
						}
						latch.countDown();
					} catch (Throwable t) {
						throwables.add(t);
					}
				}
			}).start();
		}
		
		assumeTrue(latch.await(2, TimeUnit.SECONDS));
		
		if (!throwables.isEmpty()) {
			throw throwables.poll();
		}
		
		final CountDownLatch latch2 = new CountDownLatch(STRESS_LEVEL);
		final Object result = new Object();
		
		for (int i = 0; i < STRESS_LEVEL; ++i) {
			final int start = i * stride; // 0 * 20 = 0, 1 * 20 = 20;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					int i = start;
					try {
						for (; i < start + stride; ++i) {
							
							cache.resume(pendingKeys[i], result);
							assertThat(tasks[i].result(), is(sameInstance(result)));
							verify(taskRunner).execute(tasks[i]);
						}
						latch2.countDown();
					} catch (Throwable t) {
						System.err.println(i);
						throwables.add(t);
					}
				}
			}).start();
		}
		
		assumeTrue(latch2.await(2, TimeUnit.SECONDS));
		
		if (!throwables.isEmpty()) {
			throw throwables.poll();
		}
		
		final CountDownLatch latch3 = new CountDownLatch(STRESS_LEVEL);
		
		for (int i = 0; i < STRESS_LEVEL; ++i) {
			final int start = i * stride; // 0 * 20 = 0, 1 * 20 = 20;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						for (int i = start; i < start + stride; ++i) {
							assertResume(i);
						}
						latch3.countDown();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}).start();
		}
		
		assumeTrue(latch3.await(2, TimeUnit.SECONDS));
		
		if (!throwables.isEmpty()) {
			throw throwables.poll();
		}
		
	}
	
	private void assertResume(int index) {

		boolean asserted = false;
		try {
			cache.resume(pendingKeys[index], ""); // should throw!
		} catch (AssertionError ae) {
			asserted = true;
		}
		
		assertTrue(asserted);
	}

}
