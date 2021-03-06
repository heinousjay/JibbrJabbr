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

import jj.event.Listener;
import jj.event.Subscriber;


@Subscriber
public class ChildSub {
	
	public int heard = 0;
	
	public int heard2 = 0;
	
	@Listener
	static void dontListen(Event event) {
		throw new AssertionError();
	}
	
	@Listener
	void on(Event event) {
		++heard;
	}
	
	@Listener
	Object on(EventSub eventSub) {
		++heard2;
		return null;
	}
}