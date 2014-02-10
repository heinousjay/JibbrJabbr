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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Paths;

import jj.Closer;
import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.ExecutionEvent;
import jj.http.server.CurrentWebSocketConnection;
import jj.http.server.WebSocketConnection;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceNotViableException;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.script.MockRhinoContextProvider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
	@Mock ScriptCompiler scriptCompiler;
	@Mock DocumentWebSocketMessageProcessors processors;
	MockRhinoContextProvider contextMaker;
	
	@Mock WebSocketConnection connection;
	
	CurrentDocument currentDocument;
	
	CurrentWebSocketConnection currentConnection;
	
	@Captor ArgumentCaptor<ExecutionEvent> eventCaptor;

	@Before
	public void before() throws Exception {
		contextMaker = new MockRhinoContextProvider();
		given(contextMaker.context.newObject(any(Scriptable.class))).willReturn(local);
		
		given(script.path()).willReturn(Paths.get("/"));
		
		currentDocument = new CurrentDocument();
		
		currentConnection = new CurrentWebSocketConnection();
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
	
	private DocumentScriptEnvironment givenADocumentScriptEnvironment(String baseName) {
		return new DocumentScriptEnvironment(
			cacheKey,
			baseName,
			resourceFinder,
			contextMaker,
			api,
			publisher,
			scriptCompiler,
			processors,
			currentDocument,
			currentConnection
		);
	}
	
	@Test
	public void testManagesContext() throws Exception {
		
		String key = "key";
		String name = "index";
		
		givenAnHtmlResource(name);
		given(html.document()).willReturn(Jsoup.parse("<html><head><title>test</title></head></html>"));
		DocumentScriptEnvironment dse = givenADocumentScriptEnvironment(name);
		
		// we expect documents to be cloned on first access, and that instance should be maintained
		Document document = dse.document();
		
		try (Closer closer = currentDocument.enterScope(document)) {
			dse.captureContextForKey(key);
		}
		
		assertThat(currentDocument.current(), is(nullValue()));
		
		try (Closer closer = dse.restoreContextForKey(key)) {
			assertThat(currentDocument.current(), is(sameInstance(document)));
		}

		assertThat(currentDocument.current(), is(nullValue()));
		
		try (Closer closer = currentConnection.enterScope(connection)) {
			dse.captureContextForKey(key);
		}
		
		assertThat(currentConnection.current(), is(nullValue()));
		
		try (Closer closer = dse.restoreContextForKey(key)) {
			assertThat(currentConnection.current(), is(sameInstance(connection)));
		}
		
		assertThat(currentConnection.current(), is(nullValue()));
	}
	
	@Test
	public void testThrowsNotFound() throws Exception {
		
		String baseName = "index";
		
		try {
			givenADocumentScriptEnvironment(baseName);
			fail();
		} catch (NoSuchResourceException nsre) {
			assertThat(nsre.getMessage(), is(baseName + "-" + baseName + ".html"));
		}
	}

	@Test
	public void testCompilationFailureHandledCorrectly() throws Exception {
		
		String name = "broken";
		
		givenAnHtmlResource(name);
		givenASharedScript(name);
		givenAServerScript(name);
		
		RuntimeException re = new RuntimeException();
		
		given(scriptCompiler.compile(local, null, script, script)).willThrow(re);
		
		try {
			givenADocumentScriptEnvironment(name);
			fail();
		} catch (ResourceNotViableException rnve) {
			assertThat(rnve.getMessage(), is(name));
			assertThat(rnve.getCause(), is(sameInstance((Throwable)re)));
		}
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
		
		givenADocumentScriptEnvironment(name);
		
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
		
		DocumentScriptEnvironment result = givenADocumentScriptEnvironment(name);
		
		verify(html).addDependent(result);
		verify(script, times(3)).addDependent(result);
		
		assertThat(result.sha1().length(), is(40));
		assertThat(result.uri(), is("/" + result.sha1() + "/" + name));
		assertThat(result.socketUri(), is("/" + result.sha1() + "/" + name + ".socket"));
	}
	
	@Test
	public void testHTMLWithNoServerScript() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAClientScript(name);
		givenASharedScript(name);
		
		DocumentScriptEnvironment result = givenADocumentScriptEnvironment(name);;
		
		assertThat(result.sha1().length(), is(40));
		assertThat(result.uri(), is("/" + result.sha1() + "/" + name));
		assertThat(result.socketUri(), is(nullValue()));
		
		assertThat(result.scope(), is(nullValue()));
		assertThat(result.script(), is(nullValue()));
	}
}
