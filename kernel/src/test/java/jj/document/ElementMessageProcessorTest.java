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

import static org.mockito.BDDMockito.*;
import jj.document.ElementMessageProcessor;
import jj.engine.EventSelection;
import jj.http.server.WebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.jjmessage.MessageMaker;
import jj.script.ContinuationCoordinator;
import jj.script.ContinuationPendingKey;

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
public class ElementMessageProcessorTest {
	
	@Mock ContinuationCoordinator continuationCoordinator;

	@Mock WebSocketConnection connection;
	
	@InjectMocks ElementMessageProcessor emp;
	
	@Test
	public void test() {
		
		//given
		JJMessage jqm = MessageMaker.makeElement("id", "selector");
		jqm.pendingKey(new ContinuationPendingKey());
		
		//when
		emp.handle(connection, jqm);
		
		//then
		verify(continuationCoordinator).resume(eq(jqm.pendingKey()), isA(EventSelection.class));
	}

}
