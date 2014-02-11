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

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.*;
import jj.engine.EventSelection;
import jj.execution.JJExecutor;
import jj.http.server.WebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.jjmessage.MessageMaker;
import jj.resource.document.ElementMessageProcessor;


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
public class ElementMessageProcessorTest {

	@Mock JJExecutor executor;
	@Mock WebSocketConnection connection;
	
	@InjectMocks ElementMessageProcessor emp;
	
	@Captor ArgumentCaptor<EventSelection> eventSelection;
	
	@Test
	public void test() {
		
		//given
		JJMessage jqm = MessageMaker.makeElement("id", "selector");
		
		//when
		emp.handle(connection, jqm);
		
		//then
		verify(executor).resume(eq(jqm.pendingKey()), eventSelection.capture());
		
		// this is goofy
		assertThat(eventSelection.getValue(), is(notNullValue()));
	}

}
