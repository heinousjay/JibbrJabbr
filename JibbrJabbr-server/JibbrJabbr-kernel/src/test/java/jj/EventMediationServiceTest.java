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
import static org.junit.Assert.fail;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jj.api.Event;
import jj.api.NonBlocking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

/**
 * @author jason
 *
 */
public class EventMediationServiceTest {
	
	/**
	 * Tunable delay to allow the thread to do
	 * its work.  no real way to wait for it.
	 */
	private static final int DELAY = 10;

	@Event
	public enum TestEvent1 {
		TYPE_1,
		TYPE_2
	}
	
	@Event
	public class TestEvent2 {
		final int value;
		
		TestEvent2(final int value) {
			this.value = value;
		}
	}
	
	TestThreadPool ttp;
	EventMediationService ems;
	
	AtomicInteger count;
	AtomicBoolean registered;
	StringBuffer failedMessages;
	AtomicReference<CountDownLatch> cdl;
	
	class Sink1 {
		@NonBlocking 
		public void wheee(TestEvent1 event) {
			count.incrementAndGet();
			if (registered.get()) {
				cdl.get().countDown();
			} else {
				failedMessages.append("wheee called when not registered\n");
			}
		}
		
		@NonBlocking
		public void yay(TestEvent1 event) {
			count.incrementAndGet();
			if (registered.get()) {
				cdl.get().countDown();
			} else {
				failedMessages.append("yay called when not registered\n");
			}
		}
		
		@NonBlocking
		public void what(TestEvent2 event) {
			count.incrementAndGet();
			if (registered.get()) {
				cdl.get().countDown();
			} else {
				failedMessages.append("what called when not registered\n");
			}
		}
	}
	
	class Sink2 {
		
		final int expected;
		
		Sink2(final int expected) {
			this.expected = expected;
		}
		
		@NonBlocking
		public void hey(TestEvent2 event) {
			if (event.value != expected) {
				failedMessages.append("expected value <" + expected + ">, received <" + event.value + ">");
			}
		}
	}
	
	
	@Before
	public void before() {
		MessageConveyor messageConveyor = new MessageConveyor(Locale.US);
		LocLogger logger = new LocLogger(new MockLogger(), messageConveyor);
		ttp = new TestThreadPool();
		ems = new EventMediationService(ttp, logger, messageConveyor);
		
		count = new AtomicInteger(0);
		registered = new AtomicBoolean(false);
		failedMessages = new StringBuffer();
		cdl = new AtomicReference<>();
	}
	
	@After
	public void after() {
		ems.publish(KernelControl.Dispose);
		ttp.shutdownNow();
	}

	@Test
	public void testBasicBehavior() throws Exception {
		
		Sink1 sink = new Sink1();
		
		cdl.set(new CountDownLatch(4));
		
		ems.register(sink);
		registered.set(true);
		ems.publish(TestEvent1.TYPE_1);
		ems.publish(TestEvent1.TYPE_2);
		
		if (!cdl.get().await(2, SECONDS)) {
			fail("not all events were received");
		}
		
		ems.unregister(sink);
		registered.set(false);
		ems.publish(TestEvent1.TYPE_1);
		ems.publish(TestEvent1.TYPE_2);
		
		// we need to pause here because there is no way to test for a call that
		// isn't supposed to happen :D
		Thread.sleep(100);
		if (failedMessages.length() > 0) {
			fail(failedMessages.toString());
		}
		
		if (count.get() != 4) {
			fail("wrong number of calls");
		}
	}
	
	@Test
	public void testRegistrationCycles() throws Exception {
		
		Sink1 sink = new Sink1();
		
		for (int i = 0; i < 10; ++i) {
		
			cdl.set(new CountDownLatch(4));
			count.set(0);
			
			ems.register(sink);
			registered.set(true);
			ems.publish(TestEvent1.TYPE_1);
			ems.publish(TestEvent1.TYPE_2);
			
			if (!cdl.get().await(2, SECONDS)) {
				fail("not all events were received");
			}
			
			ems.unregister(sink);
			registered.set(false);
			ems.publish(TestEvent1.TYPE_1);
			ems.publish(TestEvent1.TYPE_2);
			
			// we need to pause here because there is no way to test for a call that
			// isn't supposed to happen :D
			Thread.sleep(DELAY);
			if (failedMessages.length() > 0) {
				fail(failedMessages.toString());
			}
			
			if (count.get() != 4) {
				fail("wrong number of calls");
			}
		}
	}
	
	@Test
	public void testCommunication() throws Exception {
		
		int value = 4;
		
		Sink2 sink2 = new Sink2(value);
		
		ems.register(sink2);
		ems.publish(new TestEvent2(4));
		
		Thread.sleep(DELAY);
		if (failedMessages.length() > 0) {
			fail(failedMessages.toString());
		}
	}

}
