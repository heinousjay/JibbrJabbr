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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Paths;
import java.util.HashSet;

import jj.application.AppLocation;
import jj.document.servable.DocumentRequestProcessor;
import jj.engine.EngineAPI;
import jj.http.server.websocket.CurrentWebSocketConnection;
import jj.http.server.websocket.MockAbstractWebSocketConnectionHostDependencies;
import jj.http.server.websocket.MockCurrentWebSocketConnection;
import jj.http.server.websocket.WebSocketConnection;
import jj.http.server.websocket.WebSocketConnectionHost;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
import jj.resource.ResourceNotViableException;
import jj.script.PendingKey;
import jj.script.ScriptSystemEvent;
import jj.script.MockRhinoContextProvider;
import jj.script.module.ScriptResource;
import jj.util.Closer;

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
	@Mock EngineAPI api;
	@Mock ScriptableObject local;
	@Mock ScriptCompiler scriptCompiler;
	@Mock DocumentWebSocketMessageProcessors processors;
	@Mock DocumentRequestProcessor documentRequestProcessor;

	MockRhinoContextProvider contextMaker;
	MockAbstractWebSocketConnectionHostDependencies dependencies;
	
	@Mock WebSocketConnection connection;
	
	CurrentDocumentRequestProcessor currentDocument;
	
	CurrentWebSocketConnection currentConnection;
	
	@Captor ArgumentCaptor<ScriptSystemEvent> eventCaptor;

	@Before
	public void before() throws Exception {
		
		given(script.path()).willReturn(Paths.get("/"));
		
		currentDocument = new CurrentDocumentRequestProcessor();
		
		currentConnection = new MockCurrentWebSocketConnection();
	}
	
	private void makeResourceDependencies(String name) {
		dependencies = new MockAbstractWebSocketConnectionHostDependencies(DocumentScriptEnvironment.class, name, resourceFinder);

		contextMaker = dependencies.mockRhinoContextProvider();
		given(contextMaker.context.newObject(any(Scriptable.class))).willReturn(local);
		given(contextMaker.context.newChainedScope(any(Scriptable.class))).willReturn(local);
	}
	
	private void givenAnHtmlResource(String baseName) throws Exception {
		given(resourceFinder.loadResource(HtmlResource.class, AppLocation.Public, baseName + ".html")).willReturn(html);
	}

	private void givenAClientScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, AppLocation.Public, ScriptResourceType.Client.suffix(baseName))).willReturn(script);
	}

	private void givenASharedScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, AppLocation.Public, ScriptResourceType.Shared.suffix(baseName))).willReturn(script);
	}

	private void givenAServerScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, AppLocation.Private, ScriptResourceType.Client.suffix(baseName))).willReturn(script);
	}
	
	private DocumentScriptEnvironment givenADocumentScriptEnvironment(String baseName) {
		makeResourceDependencies(baseName);
		
		return new DocumentScriptEnvironment(
			dependencies,
			api,
			scriptCompiler,
			processors,
			currentDocument,
			currentConnection
		);
	}
	
	@Test
	public void testManagesContext() throws Exception {
		
		PendingKey key = new PendingKey();
		String name = "index";
		
		givenAnHtmlResource(name);
		given(html.document()).willReturn(Jsoup.parse("<html><head><title>test</title></head></html>"));
		DocumentScriptEnvironment dse = givenADocumentScriptEnvironment(name);
		willReturn(dse).given(connection).webSocketConnectionHost();
		// we expect documents to be cloned on first access, and that instance should be maintained
		Document document = dse.document();
		given(documentRequestProcessor.document()).willReturn(document);
		
		try (Closer ignored = currentDocument.enterScope(documentRequestProcessor)) {
			dse.captureContextForKey(key);
		}
		
		assertThat(currentDocument.current(), is(nullValue()));
		
		try (Closer ignored = dse.restoreContextForKey(key)) {
			assertThat(currentDocument.current(), is(sameInstance(documentRequestProcessor)));
		}

		assertThat(currentDocument.current(), is(nullValue()));
		
		try (Closer ignored = currentConnection.enterScope(connection)) {
			dse.captureContextForKey(key);
		}
		
		assertThat(currentConnection.current(), is(nullValue()));
		
		try (Closer ignored = dse.restoreContextForKey(key)) {
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
			assertThat(nsre.getMessage(), is(DocumentScriptEnvironment.class.getName() + "@" + baseName + "-" + baseName + ".html"));
		}
	}

	@Test
	public void testCompilationFailureHandledCorrectly() throws Exception {
		
		String name = "broken";
		
		givenAnHtmlResource(name);
		givenASharedScript(name);
		givenAServerScript(name);
		
		RuntimeException re = new RuntimeException();
		
		willThrow(re).given(scriptCompiler).compile(local, null, script, null);
		
		try {
			givenADocumentScriptEnvironment(name);
			fail("compilation failure should have happened!");
		} catch (ResourceNotViableException rnve) {
			assertThat(rnve.getMessage(), is(name));
			assertThat(rnve.getCause(), is(sameInstance((Throwable)re)));
		}
	}
	
	@Test
	public void testAllTogether() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAClientScript(name);
		given(script.source()).willReturn("");
		givenASharedScript(name);
		givenAServerScript(name);
		
		DocumentScriptEnvironment result = givenADocumentScriptEnvironment(name);
		
		verify(html).addDependent(result);
		verify(script, times(3)).addDependent(result);
		
		assertThat(result.sha1().length(), is(40));
		assertThat(result.serverPath(), is("/" + result.sha1() + "/" + name));
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
		assertThat(result.serverPath(), is("/" + result.sha1() + "/" + name));
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
		
		WebSocketConnectionHost result = givenADocumentScriptEnvironment(name);
		
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
		
		return (DocumentScriptEnvironment)result;
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
		
		// TODO!! this should be externalized behavior since it will be common to all
		// web socket connection hosts!
		
		DocumentScriptEnvironment result = givenAConnectedDocumentScriptEnvironment();
		HashSet<WebSocketConnection> iterated1 = makeSet();
		HashSet<WebSocketConnection> iterated2 = makeSet();
		
		result.startBroadcasting();
		
		assertThat(result.nextConnection(), is(true));
		assertThat(iterated1.remove(result.currentConnection()), is(true));
		assertThat(result.nextConnection(), is(true));
		assertThat(iterated1.remove(result.currentConnection()), is(true));
		
		PendingKey key = new PendingKey();
		
		result.captureContextForKey(key);
		result.exitedScope();
		
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
