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

import jj.engine.HostEvent;
import jj.script.ScriptRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JJWebSocketHandlerTest {
	
	@InjectMocks JJWebSocketHandler wsh;
	@Mock JJWebSocketConnection connection;
	@Mock ScriptRunner scriptRunner;

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

}
