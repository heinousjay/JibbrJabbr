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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import jj.execution.MockJJExecutor;
import jj.jjmessage.EventNameHelper;
import jj.script.ContinuationCoordinator;

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
	MockJJExecutor executor;
	@Mock ContinuationCoordinator continuationCoordinator;
	@Mock Callable eventFunction;
	@Mock Callable eventFunction2;
	
	ConnectionEventExecutor cee;
	
	@Before
	public void before() {
		executor = new MockJJExecutor();
		
		cee = new ConnectionEventExecutor(executor, continuationCoordinator, new MockCurrentWebSocketConnection());
		
		given(connection.webSocketConnectionHost()).willReturn(webSocketConnectionHost);
	}
	
	@Test
	public void testWebSocketEventDelegationToGlobal() throws Exception {
		
		// given 
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(webSocketConnectionHost.getFunction(eventName)).willReturn(eventFunction);
		given(connection.getFunction(eventName)).willReturn(null);
		
		// when
		cee.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(webSocketConnectionHost, eventFunction);
		verify(connection).getFunction(eventName);
		verify(webSocketConnectionHost).getFunction(eventName);

		//given
		eventName = EventNameHelper.makeEventName("jason", "miller", "rocks");
		given(webSocketConnectionHost.getFunction(eventName)).willReturn(eventFunction2);
		given(connection.getFunction(eventName)).willReturn(eventFunction);
		
		// when
		cee.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).execute(webSocketConnectionHost, eventFunction);
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
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(webSocketConnectionHost, eventFunction);
	}

}
