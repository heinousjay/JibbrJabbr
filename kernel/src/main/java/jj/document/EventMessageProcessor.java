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
package jj.document;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.ScriptableObject;

import jj.engine.EventSelection;
import jj.http.server.websocket.ConnectionEventExecutor;
import jj.http.server.websocket.CurrentWebSocketConnection;
import jj.http.server.websocket.WebSocketConnection;
import jj.jjmessage.EventNameHelper;
import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptJSON;
import jj.util.StringUtils;

/**
 * @author jason
 *
 */
@Singleton
class EventMessageProcessor implements DocumentWebSocketMessageProcessor {

	private final ConnectionEventExecutor executor;
	private final CurrentWebSocketConnection currentConnection;
	private final CurrentScriptEnvironment env;
	private final ScriptJSON scriptJSON;
	
	@Inject
	EventMessageProcessor(
		final ConnectionEventExecutor executor,
		final CurrentWebSocketConnection currentConnection,
		final CurrentScriptEnvironment env,
		final ScriptJSON scriptJSON
	) {
		this.executor = executor;
		this.currentConnection = currentConnection;
		this.env = env;
		this.scriptJSON = scriptJSON;
	}

	@Override
	public void handle(WebSocketConnection connection, JJMessage message) {
		
		ScriptableObject event = connection.webSocketConnectionHost().newObject();
		
		EventSelection target = new EventSelection(message.event().target, currentConnection, env);
		event.defineProperty("target", target, ScriptableObject.CONST);
		if (!StringUtils.isEmpty(message.event().form)) {
			event.defineProperty("form", scriptJSON.parse(message.event().form), ScriptableObject.CONST);
		}
		
		executor.submit(connection, EventNameHelper.makeEventName(message), event);
	}

}
