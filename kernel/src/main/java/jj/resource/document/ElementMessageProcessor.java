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
package jj.resource.document;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.EventSelection;
import jj.execution.TaskRunner;
import jj.http.server.CurrentWebSocketConnection;
import jj.http.server.WebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptEnvironment;

/**
 * handles an element response from the client, which can happen in
 * response to creation at the moment
 * @author jason
 *
 */
@Singleton
class ElementMessageProcessor implements DocumentWebSocketMessageProcessor {

	private final TaskRunner taskRunner;
	
	private final CurrentWebSocketConnection currentConnection;
	
	private final CurrentScriptEnvironment env;
	
	@Inject
	ElementMessageProcessor(final TaskRunner taskRunner, final CurrentWebSocketConnection connection, final CurrentScriptEnvironment env) {
		this.taskRunner = taskRunner;
		this.currentConnection = connection;
		this.env = env;
	}

	@Override
	public void handle(WebSocketConnection connection, JJMessage message) {
		taskRunner.resume(message.pendingKey(), new EventSelection(message.element().selector, currentConnection, env));
	}

}
