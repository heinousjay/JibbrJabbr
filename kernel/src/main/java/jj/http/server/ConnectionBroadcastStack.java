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

import java.util.Iterator;

/**
 * Represents a subset of current connections built by some predicate,
 * 
 * 
 * @author jason
 *
 */
public class ConnectionBroadcastStack {
	
	public interface Predicate {
		boolean accept(WebSocketConnection connection);
	}
	
	private final Iterator<WebSocketConnection> iterator;
	
	private final Predicate predicate;
	
	private WebSocketConnection current;
	
	private final ConnectionBroadcastStack parent;
	
	public ConnectionBroadcastStack(final ConnectionBroadcastStack parent, final Iterator<WebSocketConnection> iterator) {
		this(parent, iterator, new Predicate() {
			
			@Override
			public boolean accept(WebSocketConnection connection) {
				return true;
			}
		});
	}

	public ConnectionBroadcastStack(final ConnectionBroadcastStack parent, final Iterator<WebSocketConnection> iterator, final Predicate predicate) {
		this.parent = parent;
		this.iterator = iterator;
		this.predicate = predicate;
	}
	
	private WebSocketConnection getNext() {
		return iterator.hasNext() ? iterator.next() : null;
	}
	
	/**
	 * the next connection in the broadcast, subsequently available from peek
	 */
	public WebSocketConnection pop() {
		if (current != null) {
			current.exitedCurrentScope();
		}
		
		while((current = getNext()) != null) {
			if (predicate.accept(current)) break;
		}
		
		return current;
	}

	/**
	 * the connection as last returned by popConnection
	 */
	public WebSocketConnection peek() {
		return current;
	}
	
	public ConnectionBroadcastStack parent() {
		return parent;
	}
}
