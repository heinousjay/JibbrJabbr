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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.help.BrokenListener;
import jj.event.help.ChildSub;
import jj.event.help.Event;
import jj.event.help.EventSub;
import jj.event.help.IEvent;
import jj.event.help.Sub;
import jj.execution.MockTaskRunner;
import jj.execution.TaskRunner;
import jj.logging.SystemLogger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
	@Mock SystemLogger logger;
	
	MockTaskRunner taskRunner;
	
	@Singleton
	public static class EventManagerChild extends EventManager {
		
		@Inject
		public EventManagerChild(final SystemLogger logger) {
			super(logger);
		}
		
		Map<Class<?>, Set<Invoker>> listenerMap;
		
		
		@Override
		void listenerMap(Map<Class<?>, Set<Invoker>> listenerMap) {
			this.listenerMap = listenerMap;
			super.listenerMap(listenerMap);
		}
	}
	
	@Test
	public void test() throws Exception {
		
		EventManagerChild pub = impl();
		
		System.gc();
		
		taskRunner.runFirstTaskInDaemon();
		// it needs some small amount of time
		Thread.sleep(100);
		// verify the listeners are all unregistered so
		// we aren't leaking memory, but we should still have
		// sets for the event types
		assertThat(pub.listenerMap.size(), is(3));
		assertThat(pub.listenerMap.get(IEvent.class), is(empty()));
		assertThat(pub.listenerMap.get(Event.class), is(empty()));
		assertThat(pub.listenerMap.get(EventSub.class), is(empty()));
		
		// and publishing should not cause any exceptions at this point
		pub.publish(new EventSub());
	}
	
	private EventManagerChild impl() {
		// given
		Injector injector = Guice.createInjector(new EventModule(), new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(SystemLogger.class).toInstance(logger);
				bind(TaskRunner.class).to(MockTaskRunner.class);
				bind(Exception.class).toInstance(toThrow);
			}
		});
		
		taskRunner = injector.getInstance(MockTaskRunner.class);
		
		EventManagerChild pub = injector.getInstance(EventManagerChild.class);
		
		// publishing with nothing listening is fine
		pub.publish(new EventSub());
		
		final BrokenListener brokenListener = injector.getInstance(BrokenListener.class);
		
		
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
		assertThat(pub.listenerMap.get(Event.class).size(), is(2));
		assertThat(pub.listenerMap.get(EventSub.class).size(), is(1));
		
		
		// and let's test the exception gets logged and the broken listener heard the events
		verify(logger, times(4)).error(anyString(), eq(toThrow));
		assertThat(brokenListener.heard, is(4));
		
		return pub;
	}

	private void write_a_concurrent_registration_and_invocation_test() {
		// perhaps loop threads that keep creating and discarding new instances,
		// while another set of threads publish a lot
		// have to figure out how to make this verifiable, though
	}
	
}
