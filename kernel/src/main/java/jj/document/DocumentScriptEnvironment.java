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

import static jj.application.AppLocation.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.inject.Inject;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.document.servable.DocumentRequestProcessor;
import jj.engine.EngineAPI;
import jj.execution.ExecutionInstance;
import jj.execution.ExecutionLifecycleAware;
import jj.http.server.ServableResourceConfiguration;
import jj.http.server.ServableResource;
import jj.http.server.websocket.AbstractWebSocketConnectionHost;
import jj.http.server.websocket.ConnectionBroadcastStack;
import jj.http.server.websocket.CurrentWebSocketConnection;
import jj.http.server.websocket.WebSocketConnection;
import jj.http.server.websocket.WebSocketMessageProcessor;
import jj.resource.Location;
import jj.resource.ResourceThread;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceNotViableException;
import jj.script.PendingKey;
import jj.script.ScriptThread;
import jj.script.module.RootScriptEnvironment;
import jj.script.module.ScriptResource;
import jj.util.Closer;
import jj.util.SHA1Helper;

/**
 * Represents a document script, and manages all of the attendant resources
 * and web socket connections
 * 
 * @author jason
 *
 */
@ServableResourceConfiguration(
	name = "document",
	processor = DocumentScriptEnvironmentRouteProcessor.class
)
public class DocumentScriptEnvironment
	extends AbstractWebSocketConnectionHost
	implements ExecutionLifecycleAware, RootScriptEnvironment, ServableResource {
	
	public static final String JJ_JS = "jj.js";
	public static final String JQUERY_JS_DEV = "jquery-2.0.3.js";
	public static final String JQUERY_JS = "jquery-2.0.3.min.js";
	public static final String JQUERY_JS_MAP = "jquery-2.0.3.min.map";
	
	public static final String READY_FUNCTION_KEY = "Document.ready";
	
	// --- implementation
	
	private final HashMap<String, Callable> functions = new HashMap<>(4);
	
	private final String socketUri;
	
	private final String sha1;
	
	private final String serverPath;
	
	private final ScriptableObject scope;
	
	private final ScriptableObject global;
	
	private final HtmlResource html;
	
	private final ScriptResource clientScript;
	private final ScriptResource sharedScript;
	private final ScriptResource serverScript;
	
	private final WebSocketMessageProcessor processor;
	
	private final CurrentDocumentRequestProcessor currentDocument;
	
	private final CurrentWebSocketConnection currentConnection;
	
	private final HashMap<PendingKey, Context<?>> contexts = new HashMap<>(10);
	
	@Inject
	DocumentScriptEnvironment(
		final Dependencies dependencies,
		final EngineAPI api,
		final ScriptCompiler compiler,
		final WebSocketMessageProcessor processor,
		final CurrentDocumentRequestProcessor currentDocument,
		final CurrentWebSocketConnection currentConnection
	) {
		super(dependencies);
		
		html = resourceFinder.loadResource(HtmlResource.class, AppBase, resourceName(name));
		
		if (html == null) {
			throw new NoSuchResourceException(getClass(), name + "-" + resourceName(name));
		}
		
		clientScript = resourceFinder.loadResource(ScriptResource.class, AppBase, ScriptResourceType.Client.suffix(name));
		sharedScript = resourceFinder.loadResource(ScriptResource.class, AppBase, ScriptResourceType.Shared.suffix(name));
		serverScript = resourceFinder.loadResource(ScriptResource.class, AppBase, ScriptResourceType.Server.suffix(name));
		
		sha1 = SHA1Helper.keyFor(
			html.sha1(),
			clientScript == null ? "none" : clientScript.sha1(),
			sharedScript == null ? "none" : sharedScript.sha1(),
			serverScript == null ? "none" : serverScript.sha1()
		);

		serverPath = "/" + sha1 + "/" + name;
		
		if (serverScript == null)  {
			socketUri = null;
			global = null;
			scope = null;
		} else {
			socketUri = serverPath + ".socket";
			global = api.global();
			scope = configureTimers(configureModuleObjects(name, createChainedScope(global)));
			
			try {
				compiler.compile(scope, clientScript, sharedScript, serverScript.name());
			} catch (Exception e) {
				throw new ResourceNotViableException(name, e);
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
	protected String extension() {
		return "html";
	}

	private String resourceName(final String name) {
		return name + ".html";
	}

	@Override
	public String scriptName() {
		return ScriptResourceType.Client.suffix(name);
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public String serverPath() {
		return serverPath;
	}
	
	@Override
	public boolean safeToServe() {
		return true;
	}

	@Override
	public String contentType() {
		return settings.contentType();
	}
	
	@Override
	public boolean compressible() {
		return settings.compressible();
	}
	
	@Override
	public Charset charset() {
		return settings.charset();
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
	public Location moduleLocation() {
		return AppBase;
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
	public void enteredScope() {
		// nothing to do
	}
	
	@Override
	public void exitedScope() {
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
		
		final ExecutionInstance<T> source;
		final T current;
		final ConnectionBroadcastStack broadcastStack;
		
		Context(final ConnectionBroadcastStack broadcastStack) {
			this.source = null;
			this.current = null;
			this.broadcastStack = broadcastStack;
		}
		
		Context(final ExecutionInstance<T> source, T resource, final ConnectionBroadcastStack broadcastStack) {
			this.source = source;
			this.current = resource;
			this.broadcastStack = broadcastStack;
		}
		
		public Closer enterContext() {
			return source != null ? source.enterScope(current) : null;
		}
	}
	
	@Override
	protected void captureContextForKey(PendingKey key) {
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
	protected Closer restoreContextForKey(PendingKey key) {
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
