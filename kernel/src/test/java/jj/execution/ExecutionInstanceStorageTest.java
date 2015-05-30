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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jj.util.Closer;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ExecutionInstanceStorageTest {

	ExecutionInstanceStorage eis;
	
	@Before
	public void before() {
		eis = new ExecutionInstanceStorage();
	}
	
	CountDownLatch latch;
	
	@Test
	public void test() throws Exception {
		int count = 12;
		latch = new CountDownLatch(count);
		
		final PausedExecutionStorage[] handles = new PausedExecutionStorage[count];
		
		ExecutorService executor = Executors.newFixedThreadPool(count, (r) -> {
			Thread thread = new Thread(r);
			thread.setUncaughtExceptionHandler((t, e) -> {
				e.printStackTrace();
			});
			thread.setDaemon(true);
			return thread;
		});
		for (int i = 0; i < count; ++i) {
			int j = i;
			executor.submit(() -> {
				String value = String.valueOf(j);
				assertThat(eis.get(String.class), is(nullValue()));
				
				eis.set(String.class, value);

				assertThat(eis.get(String.class), is(value));

				handles[j] = eis.pause();
				
				eis.clear(String.class);
				
				assertThat(eis.get(String.class), is(nullValue()));
				latch.countDown();
			});
		}
		
		assertTrue("timed out 1", latch.await(1, SECONDS));
		
		latch = new CountDownLatch(count);
		
		for (int i = 0; i < count; ++i) {
			int j = i;
			executor.submit(() -> {

				String value = String.valueOf(j);
				assertThat(eis.get(String.class), is(nullValue()));
				
				try (Closer closer = handles[j].resume()) {
					
					assertThat(eis.get(String.class), is(value));
				}
				assertThat(eis.get(String.class), is(nullValue()));
				latch.countDown();
			});
		}
		
		assertTrue("timed out 2", latch.await(1, SECONDS));
		
		// not the best but if there's a failure it gets printed and the test fails so close enough!
	}

}
