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
package jj.http;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import jj.JJExecutors;
import jj.hostapi.EventSelection;
import jj.jqmessage.JQueryMessage;
import jj.jqmessage.JQueryMessage.Type;
import jj.script.CurrentScriptContext;
import jj.script.EventNameHelper;

/**
 * @author jason
 *
 */
@Singleton
class EventMessageProcessor implements WebSocketMessageProcessor {

	private final JJExecutors executors;
	private final CurrentScriptContext context;
	
	@Inject
	EventMessageProcessor(final JJExecutors executors, final CurrentScriptContext context) {
		this.executors = executors;
		this.context = context;
	}
	
	@Override
	public Type type() {
		return Type.Event;
	}

	@Override
	public void handle(JJWebSocketConnection connection, JQueryMessage message) {
		NativeObject event = new NativeObject();
		// need to get a way to make the target into the context this for the handler
		EventSelection target = new EventSelection(message.event().target, context);
		event.defineProperty("target", target, ScriptableObject.CONST);
		executors.scriptRunner().submit(connection, EventNameHelper.makeEventName(message), event);
	}

}
