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
package jj.webbit;

import static org.mockito.BDDMockito.*;
import jj.JJExecutors;
import jj.jqmessage.JQueryMessage;
import jj.jqmessage.MessageMaker;
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
public class ElementMessageProcessorTest {

	@Mock JJExecutors executors;
	@Mock ScriptRunner scriptRunner;
	@Mock JJWebSocketConnection connection;
	
	@Test
	public void test() {
		
		//given
		given(executors.scriptRunner()).willReturn(scriptRunner);
		ElementMessageProcessor emp = new ElementMessageProcessor(executors, null);
		JQueryMessage jqm = MessageMaker.makeElement("id", "selector");
		
		//when
		emp.handle(connection, jqm);
		
		//then
		verify(scriptRunner).submitPendingResult(eq(connection), eq("id"), anyVararg());
		
	}

}
