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
package jj.resource.document;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;

import jj.engine.EventSelection;
import jj.http.server.JJWebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.jjmessage.MessageMaker;
import jj.resource.document.EventMessageProcessor;
import jj.script.CurrentScriptContext;
import jj.script.EventNameHelper;
import jj.script.ScriptJSON;
import jj.script.ScriptRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EventMessageProcessorTest {

	@Mock ScriptRunner scriptRunner;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) CurrentScriptContext context;
	@Mock ScriptJSON scriptJSON;
	
	@Mock ScriptableObject scriptableObject1;
	@Mock ScriptableObject scriptableObject2;
	
	@InjectMocks EventMessageProcessor emp;
	
	@Mock JJWebSocketConnection connection;
	
	@Captor ArgumentCaptor<EventSelection> eventSelection;
	
	@Test
	public void test() {
		
		String form = "form";
		
		//given
		JJMessage event = MessageMaker.makeEvent("selector", "type", form);
		given(context.scriptEnvironment().newObject()).willReturn(scriptableObject1);
		given(scriptJSON.parse(form)).willReturn(scriptableObject2);
		//when
		emp.handle(connection, event);
		
		//then
		verify(scriptableObject1).defineProperty(eq("target"), eventSelection.capture(), eq(ScriptableObject.CONST));
		verify(scriptableObject1).defineProperty("form", scriptableObject2, ScriptableObject.CONST);
		verify(scriptRunner).submit(connection, EventNameHelper.makeEventName(event), scriptableObject1);
		
		assertThat(eventSelection.getValue(), is(notNullValue()));
	}

}
