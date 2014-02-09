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
package jj.script;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import jj.http.server.WebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.jjmessage.JJMessage;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ContinuationPending;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CurrentScriptContextTest {
	
	String pendingKey1 = "pending key 1";
	String pendingKey2 = "pending key 2";
	

	CurrentScriptContext currentScriptContext;
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	@Mock DocumentRequestProcessor documentRequestProcessor;
	@Mock WebSocketConnection connection;
	@Mock ModuleScriptEnvironment moduleScriptEnvironment;
	@Mock RequiredModule requiredModule;
	JJMessage jjMessage;
	
	@Mock ContinuationPending continuationPending;
	@Captor ArgumentCaptor<ContinuationState> continuationStateCaptor;
	
	@Before
	public void before() {
		currentScriptContext = new CurrentScriptContext();
	}
	
	@Test
	public void testRootScriptEnvironment() {
		currentScriptContext.initialize(documentScriptEnvironment);
		currentScriptContext.initialize(requiredModule, moduleScriptEnvironment);
		
		assertThat(currentScriptContext.rootScriptEnvironment(), is((ScriptEnvironment)documentScriptEnvironment));
	}
}
