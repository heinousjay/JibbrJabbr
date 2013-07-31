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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Set;

import jj.execution.JJNioEventLoopGroup;
import jj.script.AssociatedScriptBundle;

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

	@Mock JJNioEventLoopGroup eventLoopGroup;
	@InjectMocks WebSocketConnectionTracker wsct;
	
	@Mock JJWebSocketConnection connection1;
	@Mock JJWebSocketConnection connection2;
	
	@Mock AssociatedScriptBundle scriptBundle1;
	@Mock AssociatedScriptBundle scriptBundle2;
	
	@Captor ArgumentCaptor<Runnable> activityTrackerCaptor;
	
	@Test
	public void testConnectionManagement() {

		given(connection1.associatedScriptBundle()).willReturn(scriptBundle1);
		given(connection2.associatedScriptBundle()).willReturn(scriptBundle2);
		
		wsct.addConnection(connection1);
		wsct.addConnection(connection2);
		
		Set<JJWebSocketConnection> set = wsct.forScript(scriptBundle1);
		assertThat(set.size(), is(1));
		assertThat(set.contains(connection1), is(true));
		
		set = wsct.forScript(scriptBundle2);
		assertThat(set.size(), is(1));
		assertThat(set.contains(connection2), is(true));
		
		wsct.removeConnection(connection1);
		set = wsct.forScript(scriptBundle1);
		assertTrue(set.isEmpty());
		
		wsct.removeConnection(connection2);
		set = wsct.forScript(scriptBundle2);
		assertTrue(set.isEmpty());
	}
	
	@Test
	public void testConnectionManagement2() {

		given(connection1.associatedScriptBundle()).willReturn(scriptBundle1);
		given(connection2.associatedScriptBundle()).willReturn(scriptBundle1);
		
		wsct.addConnection(connection1);
		wsct.addConnection(connection2);
		
		Set<JJWebSocketConnection> set = wsct.forScript(scriptBundle1);
		assertThat(set.size(), is(2));
		assertThat(set.contains(connection1), is(true));
		assertThat(set.contains(connection2), is(true));
		
		set = wsct.forScript(scriptBundle2);
		assertTrue(set.isEmpty());
	}
	
	@Test
	public void testActivityTracking() {

		given(connection1.associatedScriptBundle()).willReturn(scriptBundle1);
		given(connection2.associatedScriptBundle()).willReturn(scriptBundle1);
		
		// given
		wsct.addConnection(connection1);
		given(connection1.lastActivity()).willReturn(System.currentTimeMillis() - 40000);
		wsct.addConnection(connection2);
		given(connection2.lastActivity()).willReturn(System.currentTimeMillis());
		
		wsct.start();
		
		verify(eventLoopGroup).scheduleAtFixedRate(activityTrackerCaptor.capture(), eq(5L), eq(5L), eq(SECONDS));
		
		Runnable activityTracker = activityTrackerCaptor.getValue();
		
		// when
		activityTracker.run();
		
		// then
		verify(connection1).close();
		verify(connection2, never()).close();
	}
}
