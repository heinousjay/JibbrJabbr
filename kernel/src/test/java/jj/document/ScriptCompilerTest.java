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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.CoreMatchers.is;

import java.nio.file.Paths;

import jj.event.Publisher;
import jj.script.MockRhinoContextProvider;
import jj.script.module.ScriptResource;

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
	String serverScriptName = "serverScriptName";
	
	@Captor ArgumentCaptor<String> clientStubCaptor;
	
	ScriptCompiler sc;
	
	@Before
	public void before() {
		contextProvider = new MockRhinoContextProvider();
		
		given(clientScript.path()).willReturn(Paths.get("/"));
		given(clientScript.source()).willReturn(
			"var notStubbed = function() {\nwhatever\n}\n" +
			"function stubWithReturn() {\nwhatever\nreturn something;\n}\n" +
			"random stuff; is ignored;\n" +
			"function stubNoReturn() {\nwhatever\n}\n" +
			"function() {\nanonymous not stubbed\n}\n"
		);
		
		given(sharedScript.path()).willReturn(Paths.get("/"));
		given(sharedScript.source()).willReturn("invalid");
		
		sc = new ScriptCompiler(contextProvider, publisher);
	}
	
	@Test
	public void testClientScript() {

		String clientStubs = "function stubWithReturn(){return global['//doInvoke']('stubWithReturn',global['//convertArgs'](arguments));}\n" +
			"function stubNoReturn(){global['//doCall']('stubNoReturn',global['//convertArgs'](arguments));}\n";
		
		RuntimeException re = new RuntimeException();
		
		given(contextProvider.context.evaluateString(scope, clientStubs, "client stub for " + serverScriptName)).willThrow(re);
		
		try {
			sc.compile(scope, clientScript, null, serverScriptName);
			fail();
		} catch (RuntimeException caught) {
			assertThat(caught, is(re));
		}
		
		verify(contextProvider.context).evaluateString(eq(scope), clientStubCaptor.capture(), anyString());
		
		verify(contextProvider.context).evaluateString(eq(scope), anyString(), anyString());
		
		assertThat(clientStubCaptor.getValue(), is(clientStubs));
		
		verify(publisher).publish(isA(EvaluatingClientStub.class));
		verify(publisher).publish(isA(ErrorEvaluatingClientStub.class));
	}
	
	@Test
	public void testSharedScript() {
		
		RuntimeException re = new RuntimeException();
		
		given(contextProvider.context.evaluateString(scope, sharedScript.source(), sharedScript.path().toString())).willThrow(re);

		
		try {
			sc.compile(scope, null, sharedScript, serverScriptName);
			fail();
		} catch (RuntimeException caught) {
			assertThat(caught, is(re));
		}
		
		verify(publisher).publish(isA(EvaluatingSharedScript.class));
		verify(publisher).publish(isA(ErrorEvaluatingSharedScript.class));
	}
	
	@Test
	public void testAll() {
		sc.compile(scope, clientScript, sharedScript, serverScriptName);
		
		verify(publisher).publish(isA(EvaluatingClientStub.class));
		verify(publisher).publish(isA(EvaluatingSharedScript.class));
	}
}
