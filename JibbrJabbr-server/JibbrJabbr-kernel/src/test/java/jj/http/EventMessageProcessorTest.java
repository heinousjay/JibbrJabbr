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
package jj.http;

import static org.mockito.BDDMockito.*;

import jj.execution.JJExecutors;
import jj.jjmessage.JJMessage;
import jj.jjmessage.MessageMaker;
import jj.script.EventNameHelper;
import jj.script.ScriptRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EventMessageProcessorTest {

	@Mock JJExecutors executors;
	@Mock ScriptRunner scriptRunner;
	@Mock JJWebSocketConnection connection;
	
	@Test
	public void test() {
		
		//given
		given(executors.scriptRunner()).willReturn(scriptRunner);
		EventMessageProcessor emp = new EventMessageProcessor(executors, null, null);
		JJMessage event = MessageMaker.makeEvent("selector", "type");
		
		//when
		emp.handle(connection, event);
		
		//then
		verify(scriptRunner).submit(eq(connection), eq(EventNameHelper.makeEventName(event)), anyVararg());
	}

}
