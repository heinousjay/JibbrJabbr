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
import jj.execution.TaskRunner;
import jj.script.ContinuationCoordinator;
import jj.script.ScriptTask;

import org.mozilla.javascript.Callable;

/**
 * 
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
		taskRunner.execute(new ConnectionEventTask("host event " + event + " on WebSocket connection", connection.webSocketConnectionHost(), continuationCoordinator, args, connection, event));	
	}
	

	/**
	 * @author jason
	 *
	 */
	private final class ConnectionEventTask extends ScriptTask<WebSocketConnectionHost> {

		private final Object[] args;

		private final WebSocketConnection connection;

		private final String event;

		private ConnectionEventTask(
			String name,
			WebSocketConnectionHost scriptEnvironment,
			ContinuationCoordinator continuationCoordinator,
			Object[] args,
			WebSocketConnection connection,
			String event
		) {
			super(name, scriptEnvironment, continuationCoordinator);
			this.args = args;
			this.connection = connection;
			this.event = event;
		}

		@Override
		public void begin() {
			
			Callable function = connection.getFunction(event);
			// NO! is this okay?  it isn't.  need to scope this stuff to clients
			if (function == null) function = scriptEnvironment.getFunction(event);
			
			if (function == null) {
				// ???
			}
			
			try (Closer closer = currentConnection.enterScope(connection)) {
				pendingKey = continuationCoordinator.execute(scriptEnvironment, function, args);
			}
		}
	}
}
