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

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jj.execution.TaskRunner;

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
		protected HelperTask(ContinuationPendingKey pendingKey) {
			super("", null);
			this.pendingKey = pendingKey;
		}

		@Override
		protected void begin() throws Exception {
			// never gonna run
		}
		
		protected Object result() {
			return result;
		}
	}

	private @Mock TaskRunner taskRunner;
	private @InjectMocks ContinuationPendingCache cache;
	
	@Test
	public void testRemoval() throws Throwable {
		
		// given
		ContinuationPendingKey key = new ContinuationPendingKey(cache);
		
		// when
		cache.storeForContinuation(new HelperTask(key));
		cache.removePendingTasks(Collections.singletonList(key));
		
		// then
		boolean failed = false;
		try {
			cache.resume(key, "");
		} catch (AssertionError ae) {
			failed = true;
		}
		
		assertTrue(failed);
	}
	
	@Test
	public void testStoreAndResume() throws Throwable {
		//System.out.println(KEY_COUNT + " keys on " + STRESS_LEVEL + " threads");
		assertThat(KEY_COUNT % STRESS_LEVEL, is(0));
		
		HelperTask[] tasks = new HelperTask[KEY_COUNT];
		ContinuationPendingKey[] pendingKeys = new ContinuationPendingKey[KEY_COUNT];
		
		for (int i = 0; i < KEY_COUNT; ++i) {
			pendingKeys[i] = new ContinuationPendingKey(cache);
			tasks[i] = new HelperTask(pendingKeys[i]);
		}
		
		final ArrayBlockingQueue<Throwable> throwables = new ArrayBlockingQueue<>(STRESS_LEVEL);
		final CountDownLatch latch = new CountDownLatch(STRESS_LEVEL);
		final int stride = STRIDE; // 200 / 10 = 20;
		
		for (int i = 0; i < STRESS_LEVEL; ++i) {
			final int start = i * stride; // 0 * 20 = 0, 1 * 20 = 20;
			new Thread(() -> {
				try {
					for (int j = start; j < start + stride; ++j) {
						cache.storeForContinuation(tasks[j]);
						Thread.yield(); // get em all good and mixed up
					}
					latch.countDown();
				} catch (Throwable t) {
					throwables.add(t);
				}
			}).start();
		}
		
		assumeTrue(latch.await(2, TimeUnit.SECONDS));
		
		if (!throwables.isEmpty()) {
			throw throwables.poll();
		}
		
		final CountDownLatch latch2 = new CountDownLatch(STRESS_LEVEL);
		
		for (int i = 0; i < STRESS_LEVEL; ++i) {
			final int start = i * stride; // 0 * 20 = 0, 1 * 20 = 20;
			new Thread(() -> {
				try {
					for (int j = start; j < start + stride; ++j) {
						final Object result = new Object();
						cache.resume(pendingKeys[j], result);
						assertThat(tasks[j].result(), is(sameInstance(result)));
						verify(taskRunner).execute(tasks[j]);
					}
					latch2.countDown();
				} catch (Throwable t) {
					throwables.add(t);
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
			new Thread(() -> {
				try {
					for (int j = start; j < start + stride; ++j) {
						assertResume(pendingKeys[j]);
					}
					latch3.countDown();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}).start();
		}
		
		assumeTrue(latch3.await(2, TimeUnit.SECONDS));
		
		if (!throwables.isEmpty()) {
			throw throwables.poll();
		}
		
	}
	
	private void assertResume(ContinuationPendingKey pendingKey) {

		boolean asserted = false;
		try {
			cache.resume(pendingKey, ""); // should throw!
		} catch (AssertionError ae) {
			asserted = true;
		}
		
		assertTrue(asserted);
	}

}
