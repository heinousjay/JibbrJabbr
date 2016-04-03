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
package jj.event;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jj.event.help.BrokenListener;
import jj.event.help.ChildSub;
import jj.event.help.ConcreteListener;
import jj.event.help.ConcurrentSub;
import jj.event.help.Event;
import jj.event.help.EventSub;
import jj.event.help.IEvent;
import jj.event.help.NoListeners;
import jj.event.help.Sub;
import jj.event.help.UnrelatedIEvent;
import jj.execution.MockTaskRunner;
import jj.execution.TaskRunner;
import jj.util.RandomHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.spi.Message;

/**
 * doesn't really make sense to test the event stuff in isolation,
 * since that'll just get mixed up in wild details.  test that it
 * dispatches as desired and be happy
 * 
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EventSystemTest {
	
	private Injector injector;
	
	private Publisher pub;

	private EventSystemState state;
	
	private Thread publisherLoop;
	
	@Before
	public void before() throws Exception {
		injector = Guice.createInjector(new EventModule(), new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(TaskRunner.class).to(MockTaskRunner.class);
			}
		});
		

		MockTaskRunner taskRunner = injector.getInstance(MockTaskRunner.class);
		
		pub = injector.getInstance(Publisher.class);

		state = injector.getInstance(EventSystemState.class);

		publisherLoop = taskRunner.runFirstTaskInDaemon();
	}
	
	@After
	public void after() {
		publisherLoop.interrupt();
	}
	
	@Test
	public void testListenersAreRequired() throws Exception {
		
		try {
			injector.getInstance(NoListeners.class);
			fail("should not have succeeded!");
		} catch (ConfigurationException ce) {
			Collection<Message> c = ce.getErrorMessages();
			assertThat(c.size(), is(1));
			Message m = c.iterator().next();
			assertThat(m.getMessage(), is(NoListeners.class.getName() + " is annotated as a @Subscriber but has no @Listener methods"));
		}
	}
	
	@Test
	public void testBrokenListener() {
		
		injector.getInstance(BrokenListener.class);
		boolean worked = false;
		try {
			pub.publish(new Event());
			worked = true;
		} catch (AssertionError ae) {
			assertThat(ae.getMessage(), is("broken event listener in jj.event.help.BrokenListener receiving jj.event.help.Event"));
		}
		
		assertFalse(worked);
	}
	
	@Test
	public void testCorrectOperation() throws Exception {
		
		Publisher pub = impl();
		
		System.gc();
		
		// it needs some small amount of time
		Thread.sleep(100);
		// verify the listeners are all unregistered so
		// we aren't leaking memory, but we should still have
		// sets for the event types
		assertThat("should have no IEvent listeners", state.listenerCountFor(IEvent.class), is(0));
		assertThat("should have no Event listeners", state.listenerCountFor(Event.class), is(0));
		assertThat("should have no EventSub listeners", state.listenerCountFor(EventSub.class), is(0));
		assertThat("should have no UnrelatedIEvent listeners", state.listenerCountFor(UnrelatedIEvent.class), is(0));
		
		// and publishing should not cause any exceptions at this point
		pub.publish(new EventSub());
	}
	
	private Publisher impl() {
		// publishing with nothing listening is fine
		pub.publish(new EventSub());
		
		
		Sub sub = injector.getInstance(Sub.class);
		ConcreteListener cl = injector.getInstance(ConcreteListener.class);
		
		// when
		pub.publish(new Event());
		pub.publish(new Event());
		
		// then
		assertThat(sub.heard, is(2));
		
		// given
		ChildSub childSub = injector.createChildInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(ChildSub.class);
			}
		}).getInstance(ChildSub.class);
		
		// when
		pub.publish(new Event());
		
		// then
		assertThat(childSub.heard, is(1));
		assertThat(childSub.heard2, is(0));
		assertThat(sub.heard, is(3));
		
		// given
		Sub sub2 = injector.getInstance(Sub.class);
		
		// when
		pub.publish(new EventSub());
		pub.publish(new UnrelatedIEvent());

		// then
		assertThat(childSub.heard, is(2));
		assertThat(childSub.heard2, is(1));
		assertThat(sub.heard, is(5));
		assertThat(sub2.heard, is(2));
		assertThat(cl.unrelatedIEventCount, is(1));
		assertThat(cl.iEventCount, is(5));
		
		// we should have four listeners registered at this point,
		// and after they go out of scope we should have none
		assertThat(state.totalListeners(), is(4));
		assertThat(state.listenerCountFor(IEvent.class), is(3));
		assertThat(state.listenerCountFor(Event.class), is(1));
		assertThat(state.listenerCountFor(EventSub.class), is(1));
		assertThat(state.listenerCountFor(UnrelatedIEvent.class), is(1));
		
		return pub;
	}
	
	@Test
	public void concurrencyTest() throws Exception {
		// thread loops 500-1000 times, on each iteration either
		// - publishing an event
		// - spawning a subscriber instance, of varying lifetime
		// occasionally the test will try to GC
		
		// this needs more assertions, but the basics make sense right now
		// we validate that a subscriber taken before any publishing receives
		// each event correctly
		
		
		final int threads = Runtime.getRuntime().availableProcessors();
		final ExecutorService executor = Executors.newFixedThreadPool(threads);
		final LinkedBlockingQueue<Throwable> throwables = new LinkedBlockingQueue<>(threads);
		final CountDownLatch latch = new CountDownLatch(threads);
		final ConcurrentSub sub = injector.getInstance(ConcurrentSub.class);
		final AtomicInteger countIEvent = new AtomicInteger();
		final AtomicInteger countEvent = new AtomicInteger();
		final AtomicInteger countEventSub = new AtomicInteger();
		
		try {
			for (int t = 0; t < threads; ++t) {
				executor.submit(() -> {
					
					try {
						int total = RandomHelper.nextInt(500, 1001);
						for (int i = 0; i < total; ++i) {
							
							if (RandomHelper.nextInt(300) == 84) { 
								// 84 was selected at random
								// (that's the joke)
								System.gc();
							}
						
							switch(RandomHelper.nextInt(10)) {
							
							case 0:
							case 1:
								injector.getInstance(Sub.class);
								break;
							case 2:
								injector.getInstance(ConcurrentSub.class);
								break;
							case 3:
							case 4:
								injector.getInstance(ChildSub.class);
							case 5:
							case 6:
								pub.publish(new EventSub());
								countIEvent.getAndIncrement();
								countEvent.getAndIncrement();
								countEventSub.getAndIncrement();
								break;
							case 7:
							case 8:
								pub.publish(new Event());
								countIEvent.getAndIncrement();
								countEvent.getAndIncrement();
								break;
							case 9:
								pub.publish(new UnrelatedIEvent());
								countIEvent.getAndIncrement();
								break;
							default:
								throw new AssertionError("you broke something");
							}
						}
						
					} catch (Throwable e) {
						throwables.add(e);
					} finally {
						latch.countDown();
					}
				});
			}
			// if there is only one cpu available, give it extra time cause it will take longer
			int seconds = Math.max(threads, 2) * 2;
			assertTrue("timed out in " + seconds + " seconds", latch.await(seconds, SECONDS));
			if (!throwables.isEmpty()) {
				AssertionError error = new AssertionError(throwables.size() + " test failures");
				Throwable t;
				while ((t = throwables.poll()) != null) {
					error.addSuppressed(t);
				}
				throw error;
			}
			
			assertThat(sub.countIEvent.get(), is(countIEvent.get()));
			assertThat(sub.countEvent.get(), is(countEvent.get()));
			assertThat(sub.countEventSub.get(), is(countEventSub.get()));
			
			
		} finally {
			executor.shutdownNow();
		}
	}

	@Ignore
	@Test
	public void stressItOut() {

		Sub sub = injector.getInstance(Sub.class);
		ConcreteListener cl = injector.getInstance(ConcreteListener.class);
		ConcurrentSub csub = injector.getInstance(ConcurrentSub.class);
		ChildSub childSub = injector.createChildInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(ChildSub.class);
			}
		}).getInstance(ChildSub.class);

		long now = System.currentTimeMillis();
		for (int i = 0; i < 100000; ++i) {
			pub.publish(new EventSub());
			pub.publish(new Event());
			pub.publish(new UnrelatedIEvent());
		}
		System.out.println(System.currentTimeMillis() - now + " ms");

		now = System.currentTimeMillis();
		for (int i = 0; i < 100000; ++i) {
			pub.publish(new EventSub());
			pub.publish(new Event());
			pub.publish(new UnrelatedIEvent());
		}
		System.out.println(System.currentTimeMillis() - now + " ms");

		now = System.currentTimeMillis();
		for (int i = 0; i < 100000; ++i) {
			pub.publish(new EventSub());
			pub.publish(new Event());
			pub.publish(new UnrelatedIEvent());
		}
		System.out.println(System.currentTimeMillis() - now + " ms");

		now = System.currentTimeMillis();
		for (int i = 0; i < 100000; ++i) {
			pub.publish(new EventSub());
			pub.publish(new Event());
			pub.publish(new UnrelatedIEvent());
		}
		System.out.println(System.currentTimeMillis() - now + " ms");

		now = System.currentTimeMillis();
		for (int i = 0; i < 100000; ++i) {
			pub.publish(new EventSub());
			pub.publish(new Event());
			pub.publish(new UnrelatedIEvent());
		}
		System.out.println(System.currentTimeMillis() - now + " ms");

		now = System.currentTimeMillis();
		for (int i = 0; i < 100000; ++i) {
			pub.publish(new EventSub());
			pub.publish(new Event());
			pub.publish(new UnrelatedIEvent());
		}
		System.out.println(System.currentTimeMillis() - now + " ms");

		now = System.currentTimeMillis();
		for (int i = 0; i < 100000; ++i) {
			pub.publish(new EventSub());
			pub.publish(new Event());
			pub.publish(new UnrelatedIEvent());
		}
		System.out.println(System.currentTimeMillis() - now + " ms");
	}
}
