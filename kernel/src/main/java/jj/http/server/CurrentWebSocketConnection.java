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

import jj.CurrentResource;
import jj.script.CurrentScriptEnvironment;

/**
 * @author jason
 *
 */
@Singleton
public class CurrentWebSocketConnection extends CurrentResource<WebSocketConnection> {

	private final CurrentScriptEnvironment env;
	
	@Inject
	CurrentWebSocketConnection(final CurrentScriptEnvironment env) {
		this.env = env;
	}
	
	public WebSocketConnection trueCurrent() {
		return resources.get();
	}
	
	@Override
	public WebSocketConnection current() {
		WebSocketConnection current = resources.get();
		if (env.currentWebSocketConnectionHost() != null && env.currentWebSocketConnectionHost().broadcasting()) {
			current = env.currentWebSocketConnectionHost().currentConnection();
		}
		return current;
	}
}
