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
package jj.event.help;

import java.util.concurrent.atomic.AtomicInteger;

import jj.event.Listener;
import jj.event.Subscriber;

/**
 * @author jason
 *
 */
@Subscriber
public class ConcurrentSub {
	
	public final AtomicInteger countIEvent = new AtomicInteger();
	public final AtomicInteger countEvent = new AtomicInteger();
	public final AtomicInteger countEventSub = new AtomicInteger();
	
	@Listener
	void on(IEvent ievent) {
		countIEvent.getAndIncrement();
	}

	@Listener
	void on(Event event) {
		countEvent.getAndIncrement();
	}
	
	@Listener
	void on(EventSub eventSub) {
		countEventSub.getAndIncrement();
	}
}
