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

import jj.Closer;
import jj.execution.JJExecutor;
import jj.execution.ScriptTask;
import jj.script.ContinuationCoordinator;

import org.mozilla.javascript.Callable;

/**
 * 
 * 
 * @author jason
 *
 */
@Singleton
public class ConnectionEventExecutor {
	
	private final JJExecutor executor;
	
	private final ContinuationCoordinator continuationCoordinator;
	
	private final CurrentWebSocketConnection currentConnection;

	@Inject
	ConnectionEventExecutor(
		final JJExecutor executor,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentWebSocketConnection currentConnection
	) {
		this.executor = executor;
		this.continuationCoordinator = continuationCoordinator;
		this.currentConnection = currentConnection;
	}
	
	public void submit(final WebSocketConnection connection, final String event, final Object...args) {
		executor.execute(new ScriptTask<WebSocketConnectionHost>("host event " + event + " on WebSocket connection", connection.webSocketConnectionHost()) {

			@Override
			public void run() {
				
				if (result == null) {
				
					Callable function = connection.getFunction(event);
					// NO! is this okay?  it isn't.  need to scope this stuff to clients
					if (function == null) function = scriptEnvironment.getFunction(event);
					try (Closer closer = currentConnection.enterScope(connection)) {
						pendingKey = continuationCoordinator.execute(scriptEnvironment, function, args);
					}
					
				} else {
					
					pendingKey = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, result);
				}
			}
		});	
	}
}
