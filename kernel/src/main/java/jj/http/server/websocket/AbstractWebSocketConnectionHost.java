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

import java.util.HashSet;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.ResourceIdentifier;
import jj.script.AbstractScriptEnvironment;
import jj.script.ScriptThread;

/**
 * @author jason
 *
 */
public abstract class AbstractWebSocketConnectionHost extends AbstractScriptEnvironment<Void> implements WebSocketConnectionHost {
	
	@Singleton
	public static class AbstractWebSocketConnectionHostDependencies {
		
		@Inject
		protected AbstractWebSocketConnectionHostDependencies() {
			
		}
	}
	
	public static class Dependencies extends AbstractScriptEnvironment.Dependencies {

		protected final AbstractWebSocketConnectionHostDependencies abstractWebSocketConnectionHostDependencies;
		
		@Inject
		protected Dependencies(
			final AbstractResourceDependencies abstractResourceDependencies,
			final AbstractScriptEnvironmentDependencies abstractScriptEnvironmentDependencies,
			final AbstractWebSocketConnectionHostDependencies abstractWebSocketConnectionHostDependencies,
			final ResourceIdentifier<?, ?> identifier
		) {
			super(
				abstractResourceDependencies,
				abstractScriptEnvironmentDependencies,
				identifier
			);
			
			this.abstractWebSocketConnectionHostDependencies = abstractWebSocketConnectionHostDependencies;
		}
	}

	protected AbstractWebSocketConnectionHost(Dependencies dependencies) {
		super(dependencies);
	}
	
	protected final HashSet<WebSocketConnection> connections = new HashSet<>(10);
	
	// this and the methods that manage it should probably go into an AbstractWebSocketConnectionHost
	// that derives from AbstractScriptEnvironment
	// it's getting kinda springframeworkesque in a hierarchy sense but it's really just mix-ins of layers
	// of functionality
	// i may wait on it until i come up with another environment that wants connections
	protected ConnectionBroadcastStack broadcastStack;
	

	
	@Override
	@ScriptThread
	public void connected(WebSocketConnection connection) {
		connections.add(connection);
		publisher.publish(new WebSocketClientConnected(connection));
	}
	
	@Override
	@ScriptThread
	public void disconnected(WebSocketConnection connection) {
		connections.remove(connection);
	}
	
	private Iterator<WebSocketConnection> iterator() {
		return new HashSet<>(connections).iterator();
	}
	
	// this stuff is a candidate for removal! it's kinda self contained.  maybe a connection
	// manager component this can just instantiate on its own
	// or maybe this can all live in the broadcastStack itself and that gets exposed?
	
	@Override
	@ScriptThread
	public void startBroadcasting() {
		broadcastStack = new ConnectionBroadcastStack(broadcastStack, iterator());
	}
	
	@Override
	@ScriptThread
	public boolean broadcasting() {
		return broadcastStack != null;
	}
	
	@Override
	@ScriptThread
	public void endBroadcasting() {
		broadcastStack = broadcastStack.parent();
	}
	
	@Override
	@ScriptThread
	public boolean nextConnection() {
		assert broadcasting();
		return broadcastStack.pop() != null;
	}
	
	@Override
	@ScriptThread
	public WebSocketConnection currentConnection() {
		WebSocketConnection result = null;
		if (broadcastStack != null) {
			result = broadcastStack.peek();
		}
		return result;
	}
}
