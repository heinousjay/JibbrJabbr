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
import static org.mockito.BDDMockito.*;

import jj.http.client.JJHttpClientRequest;
import jj.http.server.JJWebSocketConnection;
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
	
	RhinoContext rhinoContext;

	CurrentScriptContext currentScriptContext;
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	@Mock DocumentRequestProcessor documentRequestProcessor;
	@Mock JJWebSocketConnection connection;
	@Mock ModuleScriptEnvironment moduleScriptEnvironment;
	@Mock RequiredModule requiredModule;
	JJMessage jjMessage;
	
	@Mock ContinuationPending continuationPending;
	@Captor ArgumentCaptor<ContinuationState> continuationStateCaptor;
	
	@Before
	public void before() {
		MockRhinoContextMaker mrcm = new MockRhinoContextMaker();
		rhinoContext = mrcm.context;
		
		currentScriptContext = new CurrentScriptContext(mrcm);
	}
	
	@Test
	public void testRootScriptEnvironment() {
		currentScriptContext.initialize(documentScriptEnvironment);
		currentScriptContext.initialize(requiredModule, moduleScriptEnvironment);
		
		assertThat(currentScriptContext.rootScriptEnvironment(), is((ScriptEnvironment)documentScriptEnvironment));
	}
	
	@Test
	public void testAssociatedScriptExecutionEnvironmentPrepareContinuation() {
		
		// given
		currentScriptContext.initialize(documentScriptEnvironment);
		given(rhinoContext.captureContinuation()).willReturn(continuationPending);
		boolean failed = false;
		
		// when
		try {
			currentScriptContext.prepareContinuation(requiredModule);
			
		// then
			failed = true;
		} catch (AssertionError assertionError) {
			assertThat(assertionError.getMessage(), is("attempting a continuation with nothing to coordinate resumption"));
		}
		
		// and while we're here...
		currentScriptContext.end();
		assertThat(currentScriptContext.save(), is(nullValue()));
		assertThat(failed, is(false));
	}
	
	@Test
	public void testDocumentRequestProcessorPrepareContinuation() {
		
		// given
		currentScriptContext.initialize(documentRequestProcessor);
		given(rhinoContext.captureContinuation()).willReturn(continuationPending);
		given(requiredModule.pendingKey()).willReturn(pendingKey1);
		
		// when
		try {
			currentScriptContext.prepareContinuation(requiredModule);
			
		// then
			fail();
		} catch (ContinuationPending continuationPending) {
			verify(documentRequestProcessor).data(contains(pendingKey1), eq(continuationPending));
		}
		
		verify(continuationPending).setApplicationState(continuationStateCaptor.capture());
		assertThat(continuationStateCaptor.getValue().requiredModule(), is(requiredModule));
		
		// and while we're here...
		currentScriptContext.end();
		assertThat(currentScriptContext.save(), is(nullValue()));
	}
	
	@Test
	public void testRequiredModulePrepareContinuation() {
		
		// given
		currentScriptContext.initialize(requiredModule, moduleScriptEnvironment);
		given(rhinoContext.captureContinuation()).willReturn(continuationPending);
		jjMessage = JJMessage.makeRetrieve("key");
		
		// when
		try {
			currentScriptContext.prepareContinuation(jjMessage);
			
		// then
			fail();
		} catch (ContinuationPending continuationPending) {
			verify(requiredModule).data(contains(jjMessage.id()), eq(continuationPending));
		}
		
		verify(continuationPending).setApplicationState(continuationStateCaptor.capture());
		assertThat(continuationStateCaptor.getValue().jjMessage(), is(jjMessage));
		
		// and while we're here...
		currentScriptContext.end();
		assertThat(currentScriptContext.save(), is(nullValue()));
	}
	
	@Test
	public void testWebSocketConnectionPrepareContinuation() {
		
		// given
		currentScriptContext.initialize(connection);
		given(rhinoContext.captureContinuation()).willReturn(continuationPending);
		RestRequest restRequest = new RestRequest(new JJHttpClientRequest(null));
		
		// when
		try {
			currentScriptContext.prepareContinuation(restRequest);
			
		// then
			fail();
		} catch (ContinuationPending continuationPending) {
			verify(connection).data(contains(restRequest.id()), eq(continuationPending));
		}
		
		verify(continuationPending).setApplicationState(continuationStateCaptor.capture());
		assertThat(continuationStateCaptor.getValue().restRequest(), is(restRequest));
		
		// and while we're here...
		currentScriptContext.end();
		assertThat(currentScriptContext.save(), is(nullValue()));
	}

}
