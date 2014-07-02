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
package jj.document;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import jj.document.DocumentWebSocketMessageProcessor;
import jj.document.DocumentWebSocketMessageProcessors;
import jj.http.server.websocket.WebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.jjmessage.MessageMaker;
import jj.jjmessage.JJMessage.Type;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class WebSocketMessageProcessorsTest {
	
	@Mock WebSocketConnection connection;
	
	@Mock DocumentWebSocketMessageProcessor wsmp1;
	@Mock DocumentWebSocketMessageProcessor wsmp2;
	@Mock DocumentWebSocketMessageProcessor wsmp3;
	
	DocumentWebSocketMessageProcessors wsmps;
	
	@Before
	public void before() {
		Map<Type, DocumentWebSocketMessageProcessor> map = new HashMap<>();
		map.put(Type.Result, wsmp1);
		map.put(Type.Element, wsmp2);
		map.put(Type.Event, wsmp3);
		
		wsmps = new DocumentWebSocketMessageProcessors(map);
	}
	
	@Test
	public void testTextMessageReceived1() {
		JJMessage jjmessage = MessageMaker.makeResult("id", "value");
		String message = jjmessage.toString();
		assertThat(wsmps.process(connection, message), is(true));
		verify(wsmp1).handle(connection, jjmessage);
	}
	
	@Test
	public void testTextMessageReceived2() {
		JJMessage jjmessage = MessageMaker.makeEvent("selector", "type");
		String message = jjmessage.toString();
		assertThat(wsmps.process(connection, message), is(true));
		verify(wsmp3).handle(connection, jjmessage);
	}

}
