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
import static org.hamcrest.Matchers.*;

import java.util.Map;
import java.util.Set;

import jj.event.help.ChildSub;
import jj.event.help.Event;
import jj.event.help.Sub;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author jason
 *
 */
public class EventConfiguringTypeListenerTest {
	
	public static class EventManagerChild extends EventManager {
		Map<Class<?>, Set<Invoker>> listenerMap;
		
		
		@Override
		void listenerMap(Map<Class<?>, Set<Invoker>> listenerMap) {
			this.listenerMap = listenerMap;
			super.listenerMap(listenerMap);
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		
		EventManagerChild pub = impl();
		System.gc();
		// it needs some small amount of time
		Thread.sleep(200);
		// verify the listeners are all unregistered so
		// we aren't leaking memory
		assertThat(pub.listenerMap.get(Event.class), is(empty()));
	}
	
	private EventManagerChild impl() {
		// given
		Injector injector = Guice.createInjector(new EventModule());
		EventManagerChild pub = injector.getInstance(EventManagerChild.class);
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
		assertThat(sub.heard, is(3));
		
		// given
		Sub sub2 = injector.getInstance(Sub.class);
		
		// when
		pub.publish(new Event());
		
		// then
		assertThat(childSub.heard, is(2));
		assertThat(sub.heard, is(4));
		assertThat(sub2.heard, is(1));
		
		// we should have three listeners registered at this point,
		// and after they go out of scope we should have none
		assertThat(pub.listenerMap.get(Event.class).size(), is(3));
		
		return pub;
	}

}
