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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Paths;

import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.ExecutionEvent;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceNotViableException;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.script.MockRhinoContextProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@RunWith(MockitoJUnitRunner.class)
public class DocumentScriptEnvironmentTest {

	@Mock HtmlResource html;
	@Mock ScriptResource script;
	@Mock ResourceFinder resourceFinder;
	@Mock Publisher publisher;
	@Mock EngineAPI api;
	@Mock ScriptableObject local;
	@Mock ResourceCacheKey cacheKey;
	MockRhinoContextProvider contextMaker;
	
	@Captor ArgumentCaptor<ExecutionEvent> eventCaptor;
	@Captor ArgumentCaptor<String> clientStubCaptor;

	@Before
	public void before() throws Exception {
		contextMaker = new MockRhinoContextProvider();
		given(contextMaker.context.newObject(any(Scriptable.class))).willReturn(local);
		
		given(script.path()).willReturn(Paths.get("/"));
	}
	
	private void givenAnHtmlResource(String baseName) throws Exception {
		given(resourceFinder.loadResource(HtmlResource.class, HtmlResourceCreator.resourceName(baseName))).willReturn(html);
	}

	private void givenAClientScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Client.suffix(baseName))).willReturn(script);
	}

	private void givenASharedScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Shared.suffix(baseName))).willReturn(script);
	}

	private void givenAServerScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName))).willReturn(script);
	}
	
	@Test
	public void testThrowsNotFound() throws Exception {
		
		String baseName = "index";
		
		try {
			new DocumentScriptEnvironment(cacheKey, baseName, resourceFinder, contextMaker, api, publisher);
			fail();
		} catch (NoSuchResourceException nsre) {
			assertThat(nsre.getMessage(), is(baseName + "-" + baseName + ".html"));
		}
	}
	
	@Test
	public void testClientScript() throws Exception {
		
		String name = "broken";
		
		givenAnHtmlResource(name);
		givenAClientScript(name);
		given(script.script()).willReturn(
			"function stubWithReturn() {\nwhatever\nreturn something;\n}\n" +
			"function stubNoReturn() {\nwhatever\n}\n" +
			"var notStubbed = function() {\nwhatever\n}\n" +
			"random stuff; is ignored;\n" +
			"function() {\nanonymous not stubbed\n}\n"
		);
		givenAServerScript(name);
		
		RuntimeException re = new RuntimeException();
		
		given(contextMaker.context.evaluateString(eq(local), anyString(), anyString())).willReturn(null).willThrow(re);
		
		try {
			new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher);
			fail();
		} catch (ResourceNotViableException rnve) {
			assertThat(rnve.getMessage(), is(name));
			assertThat(rnve.getCause(), is(sameInstance((Throwable)re)));
		}
		
		verify(contextMaker.context, times(2)).evaluateString(eq(local), clientStubCaptor.capture(), anyString());
		
		assertThat(clientStubCaptor.getValue(), is(
			"function stubWithReturn(){return global['//doInvoke']('stubWithReturn',global['//convertArgs'](arguments));}\n" +
			"function stubNoReturn(){global['//doCall']('stubNoReturn',global['//convertArgs'](arguments));}\n"
		));
		
		verify(publisher, times(2)).publish(eventCaptor.capture());
		assertThat(eventCaptor.getAllValues().get(0), is(instanceOf(EvaluatingClientStub.class)));
		assertThat(eventCaptor.getAllValues().get(1), is(instanceOf(ErrorEvaluatingClientStub.class)));
	}

	@Test
	public void testSharedScript() throws Exception {
		
		String name = "broken";
		
		givenAnHtmlResource(name);
		givenASharedScript(name);
		givenAServerScript(name);
		
		RuntimeException re = new RuntimeException();
		
		given(contextMaker.context.evaluateString(eq(local), anyString(), anyString())).willReturn(null).willThrow(re);
		
		try {
			new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher);
			fail();
		} catch (ResourceNotViableException rnve) {
			assertThat(rnve.getMessage(), is(name));
			assertThat(rnve.getCause(), is(sameInstance((Throwable)re)));
		}
		
		verify(publisher, times(2)).publish(eventCaptor.capture());
		
		assertThat(eventCaptor.getAllValues().get(0), is(instanceOf(EvaluatingSharedScript.class)));
		assertThat(eventCaptor.getAllValues().get(1), is(instanceOf(ErrorEvaluatingSharedScript.class)));
	}

	@Test
	public void testServerScript() throws Exception {
		
		String name = "broken";
		
		givenAnHtmlResource(name);
		givenASharedScript(name);
		givenAServerScript(name);
		
		RuntimeException re = new RuntimeException();
		
		given(contextMaker.context.compileString(anyString(), anyString())).willThrow(re);
		
		try {
			new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher);
			fail();
		} catch (ResourceNotViableException rnve) {
			assertThat(rnve.getMessage(), is(name));
			assertThat(rnve.getCause(), is(sameInstance((Throwable)re)));
		}
		
		verify(publisher, times(3)).publish(eventCaptor.capture());
		
		assertThat(eventCaptor.getAllValues().get(0), is(instanceOf(EvaluatingSharedScript.class)));
		assertThat(eventCaptor.getAllValues().get(1), is(instanceOf(CompilingServerScript.class)));
		assertThat(eventCaptor.getAllValues().get(2), is(instanceOf(ErrorCompilingServerScript.class)));
	}
	
	// i know this looks like a private method is being tested... and sure, it is... but the
	// job this class does needs to be rigorously specified to ensure the system runs properly
	@Test
	public void testLocalScopeSetup() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAServerScript(name);
		ScriptableObject scope = mock(ScriptableObject.class);
		given(api.global()).willReturn(scope);
		
		new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher);
		
		verify(local).setPrototype(scope);
		verify(local).setParentScope(null);
		
		// local is return as all new object results.  luckily nothing overlaps
		verify(local).defineProperty("module", local, ScriptableObject.CONST);
		verify(local).defineProperty("id", name, ScriptableObject.CONST);
		verify(local).defineProperty(eq("require"), isNull(), eq(ScriptableObject.CONST));
		
	}
	
	@Test
	public void testAllTogether() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAClientScript(name);
		given(script.script()).willReturn("");
		givenASharedScript(name);
		givenAServerScript(name);
		
		DocumentScriptEnvironment result = new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher);
		
		verify(html).addDependent(result);
		verify(script, times(3)).addDependent(result);
		
		assertThat(result.uri(), is("/da39a3ee5e6b4b0d3255bfef95601890afd80709/" + name));
		assertThat(result.socketUri(), is("/da39a3ee5e6b4b0d3255bfef95601890afd80709/" + name + ".socket"));
	}
}
