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

import static org.mockito.BDDMockito.*;
import jj.execution.JJExecutor;
import jj.http.server.WebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.jjmessage.MessageMaker;
import jj.resource.document.ResultMessageProcessor;
import jj.script.ContinuationPendingKey;
import jj.script.ScriptJSON;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultMessageProcessorTest {

	@Mock JJExecutor executor;
	@Mock ScriptJSON json;
	@Mock WebSocketConnection connection;
	
	ResultMessageProcessor rmp;
	
	@Before
	public void before() {
		
		rmp = new ResultMessageProcessor(executor, json);
	}
	
	@Test
	public void testHandle() {
		String id = "ID";
		String value = "value";
		given(json.parse(value)).willReturn(value);
		JJMessage message = MessageMaker.makeResult(id, value);
		rmp.handle(connection, message);
		
		verify(executor).resume(new ContinuationPendingKey(id), value);
	}
	
	@Test
	public void testHandle2() {
		String id = "ID";
		String value = null;
		given(json.parse(value)).willReturn(value);
		JJMessage message = MessageMaker.makeResult(id, value);
		rmp.handle(connection, message);
		
		verify(executor).resume(new ContinuationPendingKey(id), value);
	}

}
