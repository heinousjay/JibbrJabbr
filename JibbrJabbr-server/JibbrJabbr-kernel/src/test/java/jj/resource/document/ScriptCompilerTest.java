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
import static org.mockito.BDDMockito.*;
import static org.hamcrest.CoreMatchers.is;

import java.nio.file.Paths;

import jj.event.Publisher;
import jj.resource.script.ScriptResource;
import jj.script.MockRhinoContextProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ScriptCompilerTest {

	MockRhinoContextProvider contextProvider;
	@Mock Publisher publisher;
	

	@Mock ScriptableObject scope;
	@Mock ScriptResource clientScript;
	@Mock ScriptResource sharedScript;
	@Mock ScriptResource serverScript;
	
	@Captor ArgumentCaptor<String> clientStubCaptor;
	
	ScriptCompiler sc;
	
	@Before
	public void before() {
		contextProvider = new MockRhinoContextProvider();
		
		given(clientScript.path()).willReturn(Paths.get("/"));
		given(sharedScript.path()).willReturn(Paths.get("/"));
		given(serverScript.path()).willReturn(Paths.get("/"));
		
		sc = new ScriptCompiler(contextProvider, publisher);
	}
	
	@Test
	public void testClientScript() {
		
		given(clientScript.script()).willReturn(
			"function stubWithReturn() {\nwhatever\nreturn something;\n}\n" +
			"function stubNoReturn() {\nwhatever\n}\n" +
			"var notStubbed = function() {\nwhatever\n}\n" +
			"random stuff; is ignored;\n" +
			"function() {\nanonymous not stubbed\n}\n"
		);
		
		given(contextProvider.context.evaluateString(eq(scope), anyString(), anyString())).willReturn(null);
		
		sc.compile(scope, clientScript, null, serverScript);
		
		verify(contextProvider.context).evaluateString(eq(scope), clientStubCaptor.capture(), anyString());
		
		assertThat(clientStubCaptor.getValue(), is(
			"function stubWithReturn(){return global['//doInvoke']('stubWithReturn',global['//convertArgs'](arguments));}\n" +
			"function stubNoReturn(){global['//doCall']('stubNoReturn',global['//convertArgs'](arguments));}\n"
		));
		
		verify(publisher).publish(isA(EvaluatingClientStub.class));
	}
	
	@Test
	public void testClientScriptWithFailure() {
		
		given(clientScript.script()).willReturn(
			"function stubWithReturn() {\nwhatever\nreturn something;\n}\n" +
			"function stubNoReturn() {\nwhatever\n}\n" +
			"var notStubbed = function() {\nwhatever\n}\n" +
			"random stuff; is ignored;\n" +
			"function() {\nanonymous not stubbed\n}\n"
		);
		
		RuntimeException re = new RuntimeException();
		
		given(contextProvider.context.evaluateString(eq(scope), anyString(), anyString())).willThrow(re);
		
		try {
			sc.compile(scope, clientScript, null, serverScript);
			fail();
		} catch (RuntimeException caught) {
			assertThat(caught, is(re));
		}
		
		verify(contextProvider.context).evaluateString(eq(scope), clientStubCaptor.capture(), anyString());
		
		assertThat(clientStubCaptor.getValue(), is(
			"function stubWithReturn(){return global['//doInvoke']('stubWithReturn',global['//convertArgs'](arguments));}\n" +
			"function stubNoReturn(){global['//doCall']('stubNoReturn',global['//convertArgs'](arguments));}\n"
		));
		
		verify(publisher).publish(isA(EvaluatingClientStub.class));
		verify(publisher).publish(isA(ErrorEvaluatingClientStub.class));
	}
	
	@Test
	public void testSharedScript() {
		
		given(contextProvider.context.evaluateString(eq(scope), anyString(), anyString())).willReturn(null);
		
		sc.compile(scope, null, sharedScript, serverScript);
		
		verify(publisher).publish(isA(EvaluatingSharedScript.class));
	}
	
	@Test
	public void testSharedScriptWithFailure() {
		
		RuntimeException re = new RuntimeException();
		
		given(contextProvider.context.evaluateString(eq(scope), anyString(), anyString())).willThrow(re);
		
		
		try {
			sc.compile(scope, null, sharedScript, serverScript);
			fail();
		} catch (RuntimeException caught) {
			assertThat(caught, is(re));
		}
		
		verify(publisher).publish(isA(EvaluatingSharedScript.class));
		verify(publisher).publish(isA(ErrorEvaluatingSharedScript.class));
	}

}
