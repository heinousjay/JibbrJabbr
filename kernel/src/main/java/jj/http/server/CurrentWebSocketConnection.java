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

import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptEnvironment;
import jj.util.CurrentResource;

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
		// we have to do something a little special here.  the web socket connection host, if any, may
		// be broadcasting, and so in that case, we want to use its connection.
		
		WebSocketConnection current = resources.get();
		ScriptEnvironment se = env.current();
		if (se != null && 
			se instanceof WebSocketConnectionHost && 
			((WebSocketConnectionHost)se).broadcasting()
		) {
			current = ((WebSocketConnectionHost)se).currentConnection();
		}
		return current;
	}
}
