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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.TaskRunner;
import jj.script.ContinuationCoordinator;
import jj.script.ScriptTask;
import jj.util.Closer;

import org.mozilla.javascript.Callable;

/**
 * Executes a function registered as an event in the context of a connection.
 * 
 * @author jason
 *
 */
@Singleton
public class ConnectionEventExecutor {

	private final TaskRunner taskRunner;
	
	private final ContinuationCoordinator continuationCoordinator;
	
	private final CurrentWebSocketConnection currentConnection;

	@Inject
	ConnectionEventExecutor(
		final TaskRunner taskRunner,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentWebSocketConnection currentConnection
	) {
		this.taskRunner = taskRunner;
		this.continuationCoordinator = continuationCoordinator;
		this.currentConnection = currentConnection;
	}
	
	public void submit(final WebSocketConnection connection, final String event, final Object...args) {
		taskRunner.execute(
			new ConnectionEventTask(
				"host event " + event + " on WebSocket connection",
				connection,
				continuationCoordinator,
				args,
				event
			)
		);	
	}
	
	private final class ConnectionEventTask extends ScriptTask<WebSocketConnectionHost> {

		private final Object[] args;

		private final WebSocketConnection connection;

		private final String event;

		private ConnectionEventTask(
			String name,
			WebSocketConnection connection,
			ContinuationCoordinator continuationCoordinator,
			Object[] args,
			String event
		) {
			super(name, connection.webSocketConnectionHost(), continuationCoordinator);
			this.args = args;
			this.connection = connection;
			this.event = event;
		}

		@Override
		public void begin() {
			
			Callable function = connection.getFunction(event);
			
			if (function == null) function = scriptEnvironment.getFunction(event);
			
			if (function != null) {
				try (Closer closer = currentConnection.enterScope(connection)) {
					pendingKey = continuationCoordinator.execute(scriptEnvironment, function, args);
				}
			}
		}
	}
}
