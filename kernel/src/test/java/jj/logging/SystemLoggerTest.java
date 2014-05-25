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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import jj.execution.MockTaskRunner;

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

}
