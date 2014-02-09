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
import jj.script.CurrentScriptContext;

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
	
	private final CurrentScriptContext context;
	
	private final CurrentWebSocketConnection currentConnection;

	@Inject
	ConnectionEventExecutor(
		final JJExecutor executor,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentScriptContext context,
		final CurrentWebSocketConnection currentConnection
	) {
		this.executor = executor;
		this.continuationCoordinator = continuationCoordinator;
		this.context = context;
		this.currentConnection = currentConnection;
	}
	
	public void submit(final WebSocketConnection connection, final String event, final Object...args) {
		executor.execute(new ScriptTask<WebSocketConnectionHost>("host event on WebSocket connection", connection.webSocketConnectionHost()) {

			@Override
			public void run() {
				context.initialize(connection);
				Callable function = connection.getFunction(event);
				if (function == null) function = scriptEnvironment.getFunction(event);
				try (Closer closer = currentConnection.enterScope(connection)) {
					continuationCoordinator.execute(scriptEnvironment, function, args);
				} finally {
					context.end();
				}
			}
		});	
	}
}
