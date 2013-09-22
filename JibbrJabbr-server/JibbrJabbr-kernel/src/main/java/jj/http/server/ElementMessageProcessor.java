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
package jj.http.server;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.EventSelection;
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessage.Type;
import jj.script.CurrentScriptContext;
import jj.script.ScriptRunner;

/**
 * handles an element response from the client, which can happen in
 * response to creation at the moment
 * @author jason
 *
 */
@Singleton
class ElementMessageProcessor implements WebSocketMessageProcessor {

	private final ScriptRunner scriptRunner;
	
	private final CurrentScriptContext context;
	
	@Inject
	ElementMessageProcessor(final ScriptRunner scriptRunner, final CurrentScriptContext context) {
		this.scriptRunner = scriptRunner;
		this.context = context;
	}
	
	@Override
	public Type type() {
		return Type.Element;
	}

	@Override
	public void handle(JJWebSocketConnection connection, JJMessage message) {
		scriptRunner.submitPendingResult(connection, message.element().id, new EventSelection(message.element().selector, context));
	}

}
