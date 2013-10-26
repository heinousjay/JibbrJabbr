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
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessage.Type;
import jj.jjmessage.MessageMaker;
import jj.script.ScriptRunner;

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
	@Mock ScriptRunner scriptRunner;
	@Mock WebSocketMessageProcessor wsmp1;
	@Mock WebSocketMessageProcessor wsmp2;
	@Mock WebSocketMessageProcessor wsmp3;
	Set<WebSocketMessageProcessor> messageProcessors;
	
	@Before
	public void before() {
		
		messageProcessors = new HashSet<>();
		messageProcessors.add(wsmp1);
		messageProcessors.add(wsmp2);
		messageProcessors.add(wsmp3);
		
		given(wsmp1.type()).willReturn(Type.Result);
		given(wsmp2.type()).willReturn(Type.Event);
		given(wsmp3.type()).willReturn(Type.Element);
		
		wsh = new JJWebSocketHandler(scriptRunner, messageProcessors);
	}

	@Test
	public void testOpened() {
		wsh.opened(connection);
		verify(scriptRunner).submit(connection, HostEvent.clientConnected.toString(), connection);
	}
	
	@Test
	public void testClosed() {
		wsh.closed(connection);
		verify(scriptRunner).submit(connection, HostEvent.clientDisconnected.toString(), connection);
	}
	
	@Test
	public void testGibberishIgnored() {
		
		String gibberish = "this is not a message the handler can handle.";
		wsh.messageReceived(connection, gibberish);
		
		verify(wsmp1, never()).handle(eq(connection), any(JJMessage.class));
		verify(wsmp2, never()).handle(eq(connection), any(JJMessage.class));
		verify(wsmp3, never()).handle(eq(connection), any(JJMessage.class));
	}
	
	@Test
	public void testTextMessageReceived1() {
		JJMessage jjmessage = MessageMaker.makeResult("id", "value");
		String message = jjmessage.toString();
		wsh.messageReceived(connection, message);
		verify(wsmp1).handle(connection, jjmessage);
	}
	
	@Test
	public void testTextMessageReceived2() {
		JJMessage jjmessage = MessageMaker.makeEvent("selector", "type");
		String message = jjmessage.toString();
		wsh.messageReceived(connection, message);
		verify(wsmp2).handle(connection, jjmessage);
	}

}
