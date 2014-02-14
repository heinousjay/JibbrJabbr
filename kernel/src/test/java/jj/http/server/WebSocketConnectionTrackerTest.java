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
import jj.execution.ServerTask;
import jj.execution.TaskHelper;
import jj.execution.TaskRunner;
import jj.resource.document.DocumentScriptEnvironment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionTrackerTest {

	@Mock TaskRunner taskRunner;
	@InjectMocks WebSocketConnectionTracker wsct;
	
	@Mock WebSocketConnection connection1;
	@Mock WebSocketConnection connection2;
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment1;
	@Mock DocumentScriptEnvironment documentScriptEnvironment2;
	
	@Captor ArgumentCaptor<ServerTask> activityTrackerCaptor;
	
	@Test
	public void testActivityTracking() throws Exception {

		given(connection1.webSocketConnectionHost()).willReturn(documentScriptEnvironment1);
		given(connection2.webSocketConnectionHost()).willReturn(documentScriptEnvironment1);
		
		// given
		wsct.addConnection(connection1);
		given(connection1.lastActivity()).willReturn(System.currentTimeMillis() - 40000);
		wsct.addConnection(connection2);
		given(connection2.lastActivity()).willReturn(System.currentTimeMillis());
		
		wsct.start();
		
		verify(taskRunner).execute(activityTrackerCaptor.capture());
		
		ServerTask activityTracker = activityTrackerCaptor.getValue();
		
		// when
		TaskHelper.invoke(activityTracker);
		
		// then
		verify(connection1).close();
		verify(connection2, never()).close();
	}
}
