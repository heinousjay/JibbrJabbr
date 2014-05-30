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

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import jj.event.help.ChildSub;
import jj.event.help.ConcurrentSub;
import jj.event.help.Event;
import jj.event.help.EventSub;
import jj.event.help.IEvent;
import jj.event.help.Sub;
import jj.event.help.UnrelatedIEvent;
import jj.execution.MockTaskRunner;
import jj.execution.TaskRunner;
import jj.util.RandomHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
	
	final Exception toThrow = new Exception();
	
	@Singleton
	public static class PublisherChild extends PublisherImpl {
		
		
		Map<Class<?>, LinkedBlockingQueue<Invoker>> listenerMap;
		
		
		@Override
		void listenerMap(Map<Class<?>, LinkedBlockingQueue<Invoker>> listenerMap) {
			this.listenerMap = listenerMap;
			super.listenerMap(listenerMap);
		}
	}
	
	private Injector injector;
	
	private PublisherChild pub;
	
	private Thread publisherLoop;
	
	@Before
	public void before() throws Exception {
		injector = Guice.createInjector(new EventModule(), new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(TaskRunner.class).to(MockTaskRunner.class);
				bind(Exception.class).toInstance(toThrow);
			}
		});
		

		MockTaskRunner taskRunner = injector.getInstance(MockTaskRunner.class);
		
		pub = injector.getInstance(PublisherChild.class);
		
		publisherLoop = taskRunner.runFirstTaskInDaemon();
	}
	
	@After
	public void after() {
		publisherLoop.interrupt();
	}
	
	@Test
	public void testCorrectOperation() throws Exception {
		
		PublisherChild pub = impl();
		
		System.gc();
		
		// it needs some small amount of time
		Thread.sleep(100);
		// verify the listeners are all unregistered so
		// we aren't leaking memory, but we should still have
		// sets for the event types
		assertThat(pub.listenerMap.size(), is(3));
		assertTrue("should have no IEvent listeners", pub.listenerMap.get(IEvent.class).isEmpty());
		assertTrue("should have no Event listeners", pub.listenerMap.get(Event.class).isEmpty());
		assertTrue("should have no EventSub listeners", pub.listenerMap.get(EventSub.class).isEmpty());
		
		// and publishing should not cause any exceptions at this point
		pub.publish(new EventSub());
	}
	
	private PublisherChild impl() {
		// publishing with nothing listening is fine
		pub.publish(new EventSub());
		
		
		Sub sub = injector.getInstance(Sub.class);
		
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
		
		// then
		assertThat(childSub.heard, is(2));
		assertThat(childSub.heard2, is(1));
		assertThat(sub.heard, is(4));
		assertThat(sub2.heard, is(1));
		
		// we should have four listeners registered at this point,
		// and after they go out of scope we should have none
		assertThat(pub.listenerMap.size(), is(3));
		assertThat(pub.listenerMap.get(IEvent.class).size(), is(2));
		assertThat(pub.listenerMap.get(Event.class).size(), is(1));
		assertThat(pub.listenerMap.get(EventSub.class).size(), is(1));
		
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
			for (int i = 0; i < threads; ++i) {
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						
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
					}
				});
			}
			
			assertTrue("timed out", latch.await(threads * 2, SECONDS));
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
			
			System.gc();
			
			// it needs some small amount of time
			Thread.sleep(100);
			// verify the only listener still registered is the one we can reach
			assertThat(pub.listenerMap.size(), is(3));
			assertThat(pub.listenerMap.get(IEvent.class).size(), is(1));
			assertThat(pub.listenerMap.get(Event.class).size(), is(1));
			assertThat(pub.listenerMap.get(EventSub.class).size(), is(1));
			
		} finally {
			executor.shutdownNow();
		}
	}
	
	
}
