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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.HashSet;

import jj.http.server.websocket.ConnectionBroadcastStack;
import jj.http.server.websocket.WebSocketConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionBroadcastStackTest {
	
	@Mock WebSocketConnection con1;
	@Mock WebSocketConnection con2;
	@Mock WebSocketConnection con3;

	@Test
	public void test() {
		HashSet<WebSocketConnection> connections = new HashSet<>();
		connections.add(con1);
		connections.add(con2);
		connections.add(con3);
		ConnectionBroadcastStack stack = new ConnectionBroadcastStack(null, new HashSet<>(connections).iterator());
		
		WebSocketConnection current;
		while ((current = stack.pop()) != null) {
			assertTrue(connections.remove(current));
		}
		
		assertTrue(connections.isEmpty());
		
		connections.add(con1);
		connections.add(con2);
		connections.add(con3);
		stack = new ConnectionBroadcastStack(null, new HashSet<>(connections).iterator(), new ConnectionBroadcastStack.Predicate() {
			int yes = 0;
			@Override
			public boolean accept(WebSocketConnection connection) {
				return yes++ == 0;
			}
		});
		
		while ((current = stack.pop()) != null) {
			assertTrue(connections.remove(current));
		}
		
		assertThat(connections.size(), is(2));
	}

}
