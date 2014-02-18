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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jj.MockClock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ServerExecutorTest {
	
	MockClock clock;
	
	@Mock JJUncaughtExceptionHandler jjUncaughtExceptionHandler;

	ServerExecutor s;
	
	@Before
	public void before() {
		s = new ServerExecutor(clock = new MockClock(), new JJThreadFactory(jjUncaughtExceptionHandler), new JJRejectedExecutionHandler());
	}
	
	@After
	public void after() {
		s.stop();
	}
	
	private CountDownLatch latch;
	
	private Runnable helper = new Runnable() {
		
		@Override
		public void run() {
			latch.countDown();
		}
	};
	
	@Test
	public void testNoDelayExecutesRightAway() throws Exception {
		
		latch = new CountDownLatch(1);
		
		s.submit(helper, 0, TimeUnit.MILLISECONDS);
		
		assertTrue(latch.await(200, TimeUnit.MILLISECONDS));
	}
	
	@Test
	public void testDelayExecutesWhenItSays() throws Exception {
		
		latch = new CountDownLatch(1);
		
		s.submit(helper, 1, TimeUnit.MILLISECONDS);
		
		assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
		
		clock.advance();
		
		assertTrue(latch.await(50, TimeUnit.MILLISECONDS));
	}

}