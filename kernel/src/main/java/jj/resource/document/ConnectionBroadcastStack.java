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
package jj.resource.document;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jj.http.server.JJWebSocketConnection;

/**
 * Represents a subset of current connections built by some predicate,
 * 
 * 
 * @author jason
 *
 */
class ConnectionBroadcastStack {
	
	public interface Predicate {
		boolean accept(JJWebSocketConnection connection);
	}
	
	private final Iterator<JJWebSocketConnection> iterator;
	
	private final Predicate predicate;
	
	private ConnectionBroadcastStack parent;
	
	ConnectionBroadcastStack(final Set<JJWebSocketConnection> connections) {
		this(connections, new Predicate() {
			
			@Override
			public boolean accept(JJWebSocketConnection connection) {
				return true;
			}
		});
	}

	ConnectionBroadcastStack(final Set<JJWebSocketConnection> connections, final Predicate predicate) {
		iterator = new HashSet<>(connections).iterator();
		this.predicate = predicate;
	}
	
	private JJWebSocketConnection getNext() {
		return iterator.hasNext() ? iterator.next() : null;
	}
	
	public JJWebSocketConnection popConnection() {
		JJWebSocketConnection next;
		while((next = getNext()) != null) {
			if (predicate.accept(next)) break;
		}
		
		return next;
	}
	
	public void push(final ConnectionBroadcastStack parent) {
		this.parent = this;
	}
	
	public ConnectionBroadcastStack pop() {
		ConnectionBroadcastStack result = parent;
		parent = null;
		return result;
	}
}
