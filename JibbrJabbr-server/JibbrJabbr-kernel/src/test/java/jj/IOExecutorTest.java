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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class IOExecutorTest {
	
	@Spy MockTaskCreator taskCreator;
	IOExecutor ioExecutor;
	
	@Before
	public void before() {
		ioExecutor = new IOExecutor(taskCreator);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasicInteraction() throws Exception {
		final AtomicBoolean failed1 = new AtomicBoolean();
		final AtomicBoolean failed2 = new AtomicBoolean();
		final CountDownLatch latch = new CountDownLatch(2);
		final Runnable submitted = new Runnable() {
			
			@Override
			public void run() {
				failed1.set(!IOExecutor.isIOThread());
				latch.countDown();
			}
		};
		
		ioExecutor.submit(submitted);
		
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			
			@Override
			public void run() {
				failed2.set(IOExecutor.isIOThread());
				latch.countDown();
			}
		});
		
		latch.await(2, SECONDS);
		
		if (failed1.get()) {
			fail("io thread is not properly identified as io thread");
		}
		
		if (failed2.get()) {
			fail("non-io thread is improperly identified as io thread");
		}

		verify(taskCreator).newIOTask(eq(submitted), BDDMockito.any(Object.class));
		verify(taskCreator, never()).newScriptTask(BDDMockito.any(Runnable.class), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newHttpTask(BDDMockito.any(Runnable.class), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newClientTask(BDDMockito.any(Runnable.class), BDDMockito.any(RunnableScheduledFuture.class));
	}

}