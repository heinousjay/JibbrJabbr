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
package jj;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jason Miller
 *
 */
public class AsynchronousThreadPoolExecutorTest {
	
	@Rule
	public CoreResourcesRule resources = new CoreResourcesRule();

	private AsynchronousThreadPoolExecutor htpe;
	
	@Before
	public void before() {
		KernelSettings kernelSettings = new KernelSettings(resources.mockLogger(), new String[0]);
		htpe = new AsynchronousThreadPoolExecutor(resources.logger(), kernelSettings, resources.messageConveyor());
	}
	
	@After
	public void after() {
		htpe.shutdownNow();
		htpe = null;
	}
	
	@Test
	public void testThreadPoolName() {
		htpe.submit(new Runnable() {
			public void run() {
				String name = "HTTP thread 1";
				assertThat("thread named incorrectly", Thread.currentThread().getName().substring(0, name.length()), is(name));
			}
		});
	}
	
	private static final class TestRunnable implements Runnable {
		
		private final boolean fail;
		
		TestRunnable(final boolean fail) {
			this.fail = fail;
		}
		
		@Override
		public void run() {
			if (fail) {
				fail("shouldn't have run");
			} else {
				synchronized(TestRunnable.class) {
					try {
						TestRunnable.class.wait();
					} catch (InterruptedException e) {}
				}
			}
		}
	}
	
	@Test
	public void testRejectedExecutionSilentlyFails() {
		// this one is a little weird and i may change the behavior
		int maxThreads = new KernelSettings(resources.logger(), new String[0]).asynchronousThreadMaxCount();
		for (int i = 0; i < maxThreads; ++i) {
			htpe.submit(new TestRunnable(false));
		}
		
		htpe.submit(new TestRunnable(true));
	}

}
