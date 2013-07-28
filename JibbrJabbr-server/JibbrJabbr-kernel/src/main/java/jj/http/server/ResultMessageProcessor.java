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

import jj.engine.ScriptJSON;
import jj.execution.JJExecutors;
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessage.Type;

/**
 * processes incoming result messages into usable objects and restarts the
 * continuation
 * @author jason
 *
 */
@Singleton
class ResultMessageProcessor implements WebSocketMessageProcessor {

	private final JJExecutors executors;
	
	private final ScriptJSON json;
	
	@Inject
	ResultMessageProcessor(final JJExecutors executors, final ScriptJSON json) {
		this.executors = executors;
		this.json = json;
	}
	
	@Override
	public Type type() {
		return Type.Result;
	}

	@Override
	public void handle(JJWebSocketConnection connection, JJMessage message) {
		Object value = message.result().value == null ? null : json.parse(message.result().value);
		executors.scriptRunner().submitPendingResult(connection, message.result().id, value);
	}

}