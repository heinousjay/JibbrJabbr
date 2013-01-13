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
package jj.webbit;

import jj.JJExecutors;
import jj.jqmessage.JQueryMessage;
import jj.jqmessage.JQueryMessage.Type;
import jj.script.EventNameHelper;

/**
 * @author jason
 *
 */
class EventMessageProcessor implements WebSocketMessageProcessor {

	private final JJExecutors executors;
	
	EventMessageProcessor(final JJExecutors executors) {
		this.executors = executors;
	}
	
	@Override
	public Type type() {
		return Type.Event;
	}

	@Override
	public void handle(JJWebSocketConnection connection, JQueryMessage message) {
		executors.scriptRunner().submit(connection, EventNameHelper.makeEventName(message));
	}

}
