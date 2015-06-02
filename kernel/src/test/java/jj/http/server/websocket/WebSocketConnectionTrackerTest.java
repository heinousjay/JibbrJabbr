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
import static org.mockito.BDDMockito.*;
import jj.document.DocumentScriptEnvironment;
import jj.execution.DelayedExecutor.CancelKey;
import jj.execution.MockTaskRunner;
import jj.execution.ServerTask;
import jj.http.server.HttpServerStarted;
import jj.http.server.HttpServerStopped;
import jj.http.server.websocket.WebSocketConnection;
import jj.http.server.websocket.WebSocketConnectionTracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionTrackerTest {

	MockTaskRunner taskRunner;
	WebSocketConnectionTracker wsct;
	
	@Mock WebSocketConnection connection1;
	@Mock WebSocketConnection connection2;
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment1;
	@Mock DocumentScriptEnvironment documentScriptEnvironment2;
	
	@Captor ArgumentCaptor<ServerTask> activityTrackerCaptor;
	
	@Before
	public void before() {
		wsct = new WebSocketConnectionTracker((taskRunner = new MockTaskRunner()));
		
		given(connection1.webSocketConnectionHost()).willReturn(documentScriptEnvironment1);
		given(connection2.webSocketConnectionHost()).willReturn(documentScriptEnvironment1);
	}
	
	@Test
	public void testActivityTracking() throws Exception {
		
		// given
		wsct.addConnection(connection1);
		given(connection1.lastActivity()).willReturn(System.currentTimeMillis() - 40000);
		wsct.addConnection(connection2);
		given(connection2.lastActivity()).willReturn(System.currentTimeMillis());
		
		// when
		wsct.on((HttpServerStarted)null);
		ServerTask task = (ServerTask)taskRunner.runFirstTask();
		
		// then
		verify(connection1).close();
		verify(connection2, never()).close();
		
		assertThat(taskRunner.delay(task), is(5000L));
		assertTrue(taskRunner.taskWillRepeat(task));
	}
	
	@Mock CancelKey cancelKey;
	
	@Test
	public void testLifecycle() throws Exception {
		
		// given
		taskRunner.cancelKey = cancelKey;
		
		// when
		wsct.on((HttpServerStarted)null);
		ServerTask task = (ServerTask)taskRunner.runFirstTask();
		
		// then
		assertTrue(taskRunner.taskWillRepeat(task));
		
		// when
		wsct.on((HttpServerStopped)null);
		
		// then
		verify(cancelKey).cancel();
	}
}
