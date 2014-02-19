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

import org.mozilla.javascript.Undefined;

import jj.http.server.WebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.script.ContinuationCoordinator;
import jj.script.ScriptJSON;

/**
 * processes incoming result messages into usable objects and restarts the
 * continuation
 * @author jason
 *
 */
@Singleton
class ResultMessageProcessor implements DocumentWebSocketMessageProcessor {

	private final ContinuationCoordinator continuationCoordinator;
	
	private final ScriptJSON json;
	
	@Inject
	ResultMessageProcessor(final ContinuationCoordinator continuationCoordinator, final ScriptJSON json) {
		this.continuationCoordinator = continuationCoordinator;
		this.json = json;
	}

	@Override
	public void handle(WebSocketConnection connection, JJMessage message) {
		Object value = message.result().value == null ? Undefined.instance : json.parse(message.result().value);
		continuationCoordinator.resume(message.pendingKey(), value);
	}

}
