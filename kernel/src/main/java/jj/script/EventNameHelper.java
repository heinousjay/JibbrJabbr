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
package jj.script;

import jj.jjmessage.Event;
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessage.Type;

/**
 * common functions for making a consistent event name
 * 
 * @author jason
 *
 */
public class EventNameHelper {

	private static final String FORMAT = "%s-%s(%s)";
	
	public static String makeEventName(JJMessage eventMessage) {
		assert eventMessage != null && eventMessage.type() == Type.Event : "only event messages can be made into event names";
		Event event = eventMessage.event();
		return makeEventName(event.context, event.selector, event.type);
	}
	
	public static String makeEventName(String context, String selector, String type) {
		if (context == null) context = "";
		if (selector == null) selector = "";
		if (type == null) type = "";
		return String.format(FORMAT, context, selector, type);
	}
	
}
