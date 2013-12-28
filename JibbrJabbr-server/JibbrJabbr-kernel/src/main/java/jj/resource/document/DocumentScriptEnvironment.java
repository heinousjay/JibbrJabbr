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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.SHA1Helper;
import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.IOThread;
import jj.http.server.JJWebSocketConnection;
import jj.http.server.WebSocketHost;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceNotViableException;
import jj.resource.script.RootScriptEnvironment;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.script.AbstractScriptEnvironment;
import jj.script.FunctionContext;
import jj.script.RhinoContext;

/**
 * Represents a document script, and manages all of the attendant resources
 * and web socket connections
 * 
 * @author jason
 *
 */
@Singleton
public class DocumentScriptEnvironment
	extends AbstractScriptEnvironment
	implements FunctionContext, RootScriptEnvironment, WebSocketHost {

	@Override
	public String baseName() {
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
	public Script script() {
		return script;
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
	@IOThread
	public boolean needsReplacing() throws IOException {
		// this never goes out of scope on its own
		// dependency tracking handles it all 
		return false;
	}
	
	@Override
	public void connected(JJWebSocketConnection connection) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void disconnected(JJWebSocketConnection connection) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Iterator<JJWebSocketConnection> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// --- implementation
	

	private final HashMap<String, Callable> functions = new HashMap<>(4);

	private final String baseName;
	
	private final String uri;
	
	private final String socketUri;
	
	private final String sha1;
	
	private final ScriptableObject scope;
	
	private final Script script;
	
	private final HtmlResource html;
	
	private final ScriptResource clientScript;
	private final ScriptResource sharedScript;
	private final ScriptResource serverScript;
	
	/**
	 * @param cacheKey
	 */
	@Inject
	DocumentScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final String baseName,
		final ResourceFinder resourceFinder,
		final Provider<RhinoContext> contextProvider,
		final EngineAPI api,
		final Publisher publisher,
		final ScriptCompiler compiler
	) {
		super(cacheKey, publisher, contextProvider);
		this.baseName = baseName;
		
		html = resourceFinder.loadResource(HtmlResource.class, HtmlResourceCreator.resourceName(baseName));
		
		if (html == null) throw new NoSuchResourceException(baseName + "-" + baseName + ".html");
		
		clientScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Client.suffix(baseName));
		sharedScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Shared.suffix(baseName));
		serverScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
		sha1 = SHA1Helper.keyFor(
			html.sha1(),
			clientScript == null ? "none" : clientScript.sha1(),
			sharedScript == null ? "none" : sharedScript.sha1(),
			serverScript == null ? "none" : serverScript.sha1()
		);
		

		uri = "/" + sha1 + "/" + baseName;
		
		if (serverScript == null)  {
			socketUri = null;
			scope = null;
			script = null;
		} else {
			socketUri = uri + ".socket";
			scope = createLocalScope(baseName, api.global());
			
			try {
				script = compiler.compile(scope, clientScript, sharedScript, serverScript);
			} catch (Exception e) {
				throw new ResourceNotViableException(baseName, e);
			}
		}
		
		html.addDependent(this);
		if (clientScript != null) clientScript.addDependent(this);
		if (sharedScript != null) sharedScript.addDependent(this);
		if (serverScript != null) serverScript.addDependent(this);
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
