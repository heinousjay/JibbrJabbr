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

import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.configuration.resolution.AppLocation;
import jj.document.servable.DocumentRequestProcessor;
import jj.engine.EngineAPI;
import jj.http.server.websocket.AbstractWebSocketConnectionHost;
import jj.http.server.websocket.ConnectionBroadcastStack;
import jj.http.server.websocket.CurrentWebSocketConnection;
import jj.http.server.websocket.WebSocketConnection;
import jj.http.server.websocket.WebSocketMessageProcessor;
import jj.resource.ResourceThread;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
import jj.resource.ResourceNotViableException;
import jj.script.ContinuationPendingKey;
import jj.script.ScriptThread;
import jj.script.module.RootScriptEnvironment;
import jj.script.module.ScriptResource;
import jj.script.module.ScriptResourceType;
import jj.util.Closer;
import jj.util.CurrentResource;
import jj.util.CurrentResourceAware;
import jj.util.SHA1Helper;

/**
 * Represents a document script, and manages all of the attendant resources
 * and web socket connections
 * 
 * @author jason
 *
 */
@Singleton
public class DocumentScriptEnvironment
	extends AbstractWebSocketConnectionHost
	implements CurrentResourceAware, RootScriptEnvironment {
	
	public static final String READY_FUNCTION_KEY = "Document.ready";
	
	// --- implementation
	
	private final HashMap<String, Callable> functions = new HashMap<>(4);

	private final String baseName;
	
	private final String uri;
	
	private final String socketUri;
	
	private final String sha1;
	
	private final ScriptableObject scope;
	
	private final ScriptableObject global;
	
	private final HtmlResource html;
	
	private final ScriptResource clientScript;
	private final ScriptResource sharedScript;
	private final ScriptResource serverScript;
	
	private final WebSocketMessageProcessor processor;
	
	private final CurrentDocumentRequestProcessor currentDocument;
	
	private final CurrentWebSocketConnection currentConnection;
	
	private final HashMap<ContinuationPendingKey, Context<?>> contexts = new HashMap<>(10);
	
	/**
	 * @param cacheKey
	 */
	@Inject
	DocumentScriptEnvironment(
		final Dependencies dependencies,
		final String baseName,
		final ResourceFinder resourceFinder,
		final EngineAPI api,
		final ScriptCompiler compiler,
		final WebSocketMessageProcessor processor,
		final CurrentDocumentRequestProcessor currentDocument,
		final CurrentWebSocketConnection currentConnection
	) {
		super(dependencies);
		this.baseName = baseName;
		
		html = resourceFinder.loadResource(HtmlResource.class, AppLocation.Base, HtmlResourceCreator.resourceName(baseName));
		
		if (html == null) throw new NoSuchResourceException(getClass(), baseName + "-" + baseName + ".html");
		
		clientScript = resourceFinder.loadResource(ScriptResource.class, AppLocation.Base, ScriptResourceType.Client.suffix(baseName));
		sharedScript = resourceFinder.loadResource(ScriptResource.class, AppLocation.Base, ScriptResourceType.Shared.suffix(baseName));
		serverScript = resourceFinder.loadResource(ScriptResource.class, AppLocation.Base, ScriptResourceType.Server.suffix(baseName));
		
		sha1 = SHA1Helper.keyFor(
			html.sha1(),
			clientScript == null ? "none" : clientScript.sha1(),
			sharedScript == null ? "none" : sharedScript.sha1(),
			serverScript == null ? "none" : serverScript.sha1()
		);
		

		uri = "/" + sha1 + "/" + baseName;
		
		if (serverScript == null)  {
			socketUri = null;
			global = null;
			scope = null;
		} else {
			socketUri = uri + ".socket";
			global = api.global();
			scope = configureTimers(configureModuleObjects(baseName, createChainedScope(global)));
			
			try {
				compiler.compile(scope, clientScript, sharedScript, serverScript.name());
			} catch (Exception e) {
				throw new ResourceNotViableException(baseName, e);
			}
		}
		
		html.addDependent(this);
		if (clientScript != null) clientScript.addDependent(this);
		if (sharedScript != null) sharedScript.addDependent(this);
		if (serverScript != null) serverScript.addDependent(this);
		
		this.processor = processor;
		
		this.currentDocument = currentDocument;
		this.currentConnection = currentConnection;
	}

	@Override
	public String name() {
		return baseName;
	}
	
	@Override
	public String scriptName() {
		return ScriptResourceType.Client.suffix(baseName);
	}

	@Override
	public String uri() {
		return uri;
	}

	@Override
	public String sha1() {
		return sha1;
	}
	
	@Override
	public Scriptable scope() {
		return scope;
	}
	
	@Override
	public ScriptableObject global() {
		return global;
	}

	@Override
	public Script script() {
		return serverScript == null ? null : serverScript.script();
	}

	public Document document() {
		return html.document().clone();
	}

	@Override
	public Callable getFunction(String name) {
		return functions.get(name);
	}

	@Override
	public void addFunction(String name, Callable function) {
		functions.put(name, function);
	}

	@Override
	public boolean removeFunction(String name) {
		return functions.remove(name) != null;
	}

	@Override
	public boolean removeFunction(String name, Callable function) {
		return (functions.get(name) == function) && (functions.remove(name) == function);
	}
	
	public boolean hasServerScript() {
		return serverScript != null;
	}

	@Override
	@ResourceThread
	public boolean needsReplacing() throws IOException {
		// this never goes out of scope on its own
		// dependency tracking handles it all 
		return false;
	}
	
	@Override
	protected boolean removeOnReload() {
		// we're a root environment! reload away, please
		return false;
	}
	
	@Override
	public void enteredCurrentScope() {
		// nothing to do
	}
	
	@Override
	public void exitedCurrentScope() {
		// presumably, if there is still broadcasting to be done, then it's saved
		// away with continuation state
		broadcastStack = null;
	}
	
	@Override
	@ScriptThread
	public boolean message(WebSocketConnection connection, String message) {
		return processor.process(connection, message);
	}
	
	private static class Context<T> {
		
		final CurrentResource<T> source;
		final T current;
		final ConnectionBroadcastStack broadcastStack;
		
		Context(final ConnectionBroadcastStack broadcastStack) {
			this.source = null;
			this.current = null;
			this.broadcastStack = broadcastStack;
		}
		
		Context(final CurrentResource<T> source, T resource, final ConnectionBroadcastStack broadcastStack) {
			this.source = source;
			this.current = resource;
			this.broadcastStack = broadcastStack;
		}
		
		public Closer enterContext() {
			return source != null ? source.enterScope(current) : null;
		}
	}
	
	@Override
	protected void captureContextForKey(ContinuationPendingKey key) {
		assert !contexts.containsKey(key) : "cannot capture multiple times with the same key";
		// we can't have both a document and a connection, so this works out neatly...
		if (currentDocument.current() != null) {
			contexts.put(key, new Context<DocumentRequestProcessor>(currentDocument, currentDocument.current(), broadcastStack));
		} else if (currentConnection.trueCurrent() != null) {
			contexts.put(key, new Context<WebSocketConnection>(currentConnection, currentConnection.trueCurrent(), broadcastStack));
		} else {
			contexts.put(key, new Context<Void>(broadcastStack));
		}
	}
	
	@Override
	protected Closer restoreContextForKey(ContinuationPendingKey key) {
		assert broadcastStack == null : "restoring into a DocumentScriptEnvironment with a standing broadcastStack";
		Context<?> context = contexts.remove(key);
		broadcastStack = context.broadcastStack;
		Closer closer = context.enterContext();
		return (closer != null) ? closer : super.restoreContextForKey(key);
	}

	/**
	 * @return
	 */
	public String socketUri() {
		return socketUri;
	}

	/**
	 * @return
	 */
	public ScriptResource clientScriptResource() {
		return clientScript;
	}

	/**
	 * @return
	 */
	public ScriptResource sharedScriptResource() {
		return sharedScript;
	}
}
