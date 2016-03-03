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

import static org.mockito.BDDMockito.*;
import jj.execution.MockTaskRunner;
import jj.http.server.websocket.ConnectionEventExecutor;
import jj.http.server.websocket.WebSocketConnection;
import jj.http.server.websocket.WebSocketConnectionHost;
import jj.jjmessage.EventNameHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionEventExecutorTest {

	@Mock WebSocketConnection connection;
	@Mock WebSocketConnectionHost webSocketConnectionHost;
	MockTaskRunner taskRunner;
	@Mock Callable eventFunction;
	@Mock Callable eventFunction2;
	
	ConnectionEventExecutor cee;
	
	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		
		cee = new ConnectionEventExecutor(taskRunner, new MockCurrentWebSocketConnection());
		
		willReturn(webSocketConnectionHost).given(connection).webSocketConnectionHost();
	}
	
	@Test
	public void testWebSocketEventDelegationToGlobal() throws Exception {
		
		// given 
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(webSocketConnectionHost.getFunction(eventName)).willReturn(eventFunction);
		given(connection.getFunction(eventName)).willReturn(null);
		
		// when
		cee.submit(connection, eventName);
		taskRunner.runUntilIdle();
		
		// then
		verify(webSocketConnectionHost).execute(eventFunction);
		verify(connection).getFunction(eventName);
		verify(webSocketConnectionHost).getFunction(eventName);

		//given
		eventName = EventNameHelper.makeEventName("jason", "miller", "rocks");
		given(webSocketConnectionHost.getFunction(eventName)).willReturn(eventFunction2);
		given(connection.getFunction(eventName)).willReturn(eventFunction);
		
		// when
		cee.submit(connection, eventName);
		taskRunner.runUntilIdle();
		
		// then
		verify(webSocketConnectionHost, times(2)).execute(eventFunction);
		verify(connection).getFunction(eventName);
		verify(webSocketConnectionHost, never()).getFunction(eventName);
	}
	

	
	@Test
	public void testWebSocketJJMessageEventWithNoContinuation() throws Exception {
		
		// given
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(connection.getFunction(eventName)).willReturn(eventFunction);
		
		// when
		cee.submit(connection, eventName);
		taskRunner.runUntilIdle();
		
		// then
		verify(webSocketConnectionHost).execute(eventFunction);
	}

}
