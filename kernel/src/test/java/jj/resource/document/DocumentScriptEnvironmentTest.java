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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Paths;
import java.util.HashSet;

import jj.Closer;
import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.ExecutionEvent;
import jj.http.server.CurrentWebSocketConnection;
import jj.http.server.MockCurrentWebSocketConnection;
import jj.http.server.WebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;
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
	@Mock DocumentRequestProcessor documentRequestProcessor;
	MockRhinoContextProvider contextMaker;
	
	@Mock WebSocketConnection connection;
	
	CurrentDocumentRequestProcessor currentDocument;
	
	CurrentWebSocketConnection currentConnection;
	
	@Captor ArgumentCaptor<ExecutionEvent> eventCaptor;

	@Before
	public void before() throws Exception {
		contextMaker = new MockRhinoContextProvider();
		given(contextMaker.context.newObject(any(Scriptable.class))).willReturn(local);
		
		given(script.path()).willReturn(Paths.get("/"));
		
		currentDocument = new CurrentDocumentRequestProcessor();
		
		currentConnection = new MockCurrentWebSocketConnection();
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
		given(connection.webSocketConnectionHost()).willReturn(dse);
		// we expect documents to be cloned on first access, and that instance should be maintained
		Document document = dse.document();
		given(documentRequestProcessor.document()).willReturn(document);
		
		try (Closer closer = currentDocument.enterScope(documentRequestProcessor)) {
			dse.captureContextForKey(key);
		}
		
		assertThat(currentDocument.current(), is(nullValue()));
		
		try (Closer closer = dse.restoreContextForKey(key)) {
			assertThat(currentDocument.current(), is(sameInstance(documentRequestProcessor)));
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
		
		DocumentScriptEnvironment result = givenADocumentScriptEnvironment(name);
		
		assertThat(result.sha1().length(), is(40));
		assertThat(result.uri(), is("/" + result.sha1() + "/" + name));
		assertThat(result.socketUri(), is(nullValue()));
		
		assertThat(result.scope(), is(nullValue()));
		assertThat(result.script(), is(nullValue()));
	}
	
	@Mock WebSocketConnection connection1;
	@Mock WebSocketConnection connection2;
	@Mock WebSocketConnection connection3;
	@Mock WebSocketConnection connection4;
	@Mock WebSocketConnection connection5;
	
	private DocumentScriptEnvironment givenAConnectedDocumentScriptEnvironment() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAClientScript(name);
		givenAServerScript(name);
		
		DocumentScriptEnvironment result = givenADocumentScriptEnvironment(name);
		
		result.connected(connection1);
		result.connected(connection2);
		result.connected(connection3);
		result.connected(connection4);
		result.connected(connection5);
		given(connection1.webSocketConnectionHost()).willReturn(result);
		given(connection2.webSocketConnectionHost()).willReturn(result);
		given(connection3.webSocketConnectionHost()).willReturn(result);
		given(connection4.webSocketConnectionHost()).willReturn(result);
		given(connection5.webSocketConnectionHost()).willReturn(result);
		
		return result;
	}
	
	@Test
	public void testConnectionBroadcasting() throws Exception {
		
		DocumentScriptEnvironment result = givenAConnectedDocumentScriptEnvironment();

		assertThat(result.broadcasting(), is(false));
		result.startBroadcasting();
		assertThat(result.broadcasting(), is(true));
		
		assertThat(result.currentConnection(), is(nullValue()));
		
		HashSet<WebSocketConnection> iterated = new HashSet<>();
		
		while (result.nextConnection()) {
			iterated.add(result.currentConnection());
		}
		
		result.endBroadcasting();
		assertThat(result.broadcasting(), is(false));
		
		assertThat(iterated, containsInAnyOrder(connection1, connection2, connection3, connection4, connection5));
	}
	
	private HashSet<WebSocketConnection> makeSet() {
		HashSet<WebSocketConnection> result = new HashSet<>();
		result.add(connection1);
		result.add(connection2);
		result.add(connection3);
		result.add(connection4);
		result.add(connection5);
		return result;
	}
	
	@Test
	public void testConnectionBroadcastingWithContinuationAndNesting() throws Exception {
		
		DocumentScriptEnvironment result = givenAConnectedDocumentScriptEnvironment();
		HashSet<WebSocketConnection> iterated1 = makeSet();
		HashSet<WebSocketConnection> iterated2 = makeSet();
		
		result.startBroadcasting();
		
		assertThat(result.nextConnection(), is(true));
		assertThat(iterated1.remove(result.currentConnection()), is(true));
		assertThat(result.nextConnection(), is(true));
		assertThat(iterated1.remove(result.currentConnection()), is(true));
		
		String key = "key";
		
		result.captureContextForKey(key);
		result.end();
		
		assertThat(result.currentConnection(), is(nullValue()));
		
		result.restoreContextForKey(key);

		assertThat(result.nextConnection(), is(true));
		assertThat(iterated1.remove(result.currentConnection()), is(true));
		
		{
			// now we nest broadcasting duties - the broadcast function for some reason is also
			// broadcasting something
			// wrapped in its own block for clarity
			result.startBroadcasting();
	
			assertThat(result.nextConnection(), is(true));
			assertThat(iterated2.remove(result.currentConnection()), is(true));
			assertThat(result.nextConnection(), is(true));
			assertThat(iterated2.remove(result.currentConnection()), is(true));
			
			// should we continue again here? 
	
			assertThat(result.nextConnection(), is(true));
			assertThat(iterated2.remove(result.currentConnection()), is(true));
			assertThat(result.nextConnection(), is(true));
			assertThat(iterated2.remove(result.currentConnection()), is(true));
			assertThat(result.nextConnection(), is(true));
			assertThat(iterated2.remove(result.currentConnection()), is(true));
			assertThat(result.nextConnection(), is(false));
			
			result.endBroadcasting();
			// and we end that without sufferance and so forbears love
		}
		
		assertThat(result.nextConnection(), is(true));
		assertThat(iterated1.remove(result.currentConnection()), is(true));
		assertThat(result.nextConnection(), is(true));
		assertThat(iterated1.remove(result.currentConnection()), is(true));
		assertThat(result.nextConnection(), is(false));
		
		result.endBroadcasting();
		
		assertTrue(iterated1.isEmpty());
		assertTrue(iterated2.isEmpty());
	}
}
