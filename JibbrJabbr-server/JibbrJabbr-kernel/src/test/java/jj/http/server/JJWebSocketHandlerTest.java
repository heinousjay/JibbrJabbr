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

import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.Set;

import jj.engine.HostEvent;
import jj.execution.ExecutionTrace;
import jj.execution.MockJJExecutors;
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessage.Type;
import jj.jjmessage.MessageMaker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JJWebSocketHandlerTest {
	
	JJWebSocketHandler wsh;
	@Mock JJWebSocketConnection connection;
	MockJJExecutors executors;
	@Mock ExecutionTrace trace;
	@Mock WebSocketMessageProcessor wsmp1;
	@Mock WebSocketMessageProcessor wsmp2;
	Set<WebSocketMessageProcessor> messageProcessors;
	
	@Before
	public void before() {
		
		executors = new MockJJExecutors();
		messageProcessors = new HashSet<>();
		messageProcessors.add(wsmp1);
		messageProcessors.add(wsmp2);
		
		given(wsmp1.type()).willReturn(Type.Result);
		given(wsmp2.type()).willReturn(Type.Event);
		
		wsh = new JJWebSocketHandler(executors, trace, messageProcessors);
	}

	@Test
	public void testOpened() {
		wsh.opened(connection);
		verify(trace).start(connection);
		verify(executors.scriptRunner).submit(connection, HostEvent.clientConnected, connection);
	}
	
	@Test
	public void testClosed() {
		wsh.closed(connection);
		verify(trace).end(connection);
		verify(executors.scriptRunner).submit(connection, HostEvent.clientDisconnected, connection);
	}
	
	@Test
	public void testTextMessageReceived1() {
		JJMessage jjmessage = MessageMaker.makeResult("id", "value");
		String message = jjmessage.toString();
		wsh.messageReceived(connection, message);
		verify(trace).message(connection, message);
		verify(wsmp1).handle(connection, jjmessage);
	}
	
	@Test
	public void testTextMessageReceived2() {
		JJMessage jjmessage = MessageMaker.makeEvent("selector", "type");
		String message = jjmessage.toString();
		wsh.messageReceived(connection, message);
		verify(trace).message(connection, message);
		verify(wsmp2).handle(connection, jjmessage);
	}

}
