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
package jj.http.server.websocket;

import jj.script.FunctionContext;
import jj.script.ScriptEnvironment;

/**
 * @author jason
 *
 *TODO!!! replace ScriptEnvironment with Resource.  These may not be script environments, that should be explicit
 */
public interface WebSocketConnectionHost extends ScriptEnvironment, FunctionContext {

	void connected(WebSocketConnection connection);

	void disconnected(WebSocketConnection connection);
	
	boolean message(WebSocketConnection connection, String message);
	
	void startBroadcasting();
	
	boolean broadcasting();
	
	void endBroadcasting();
	
	boolean nextConnection();
	
	WebSocketConnection currentConnection();
}
