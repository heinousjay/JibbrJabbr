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
package jj.jjmessage;

import jj.jjmessage.Element;
import jj.jjmessage.Event;
import jj.jjmessage.JJMessage;
import jj.jjmessage.Result;

/**
 * Helper to create inbound messages for tests
 * 
 * @author jason
 *
 */
public class MessageMaker {

	public static JJMessage makeEvent(final String selector, final String type) {
		JJMessage result = new JJMessage();
		result.event(new Event());
		result.event().selector = selector;
		result.event().type = type;
		return result;
	}

	public static JJMessage makeEvent(final String selector, final String type, final String form) {
		JJMessage result = new JJMessage();
		result.event(new Event());
		result.event().selector = selector;
		result.event().type = type;
		result.event().form = form;
		return result;
	}
	
	public static JJMessage makeResult(final String id, final String value) {
		JJMessage result = new JJMessage();
		result.result(new Result());
		result.result().id = id;
		result.result().value = value;
		return result;
	}
	
	public static JJMessage makeElement(final String id, final String selector) {
		JJMessage result = new JJMessage();
		result.element(new Element());
		result.element().id = id;
		result.element().selector = selector;
		return result;
	}
}
