package jj.script;

import java.io.Closeable;

import javax.inject.Singleton;

import jj.DataStore;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
import jj.http.HttpRequest;
import jj.http.server.WebSocketConnection;
import jj.http.server.WebSocketConnectionHost;
import jj.http.server.servable.document.DocumentRequestProcessor;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.ContinuationPending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains the current context for a thread
 * executing a script.  basically a multimode
 * repository of information 
 * @author jason
 *
 */
@Singleton
public class CurrentScriptContext implements Closeable {
	
	private final Logger log = LoggerFactory.getLogger(CurrentScriptContext.class);
	
	/**
	 * maintains a per-thread nestable context that the ScriptRunner et al can use
	 * to keep track of what it's working for
	 */
	private final ThreadLocal<ScriptContext> currentContext = new ThreadLocal<>();
	
	public ScriptContextType type() {
		return currentContext.get() != null ? currentContext.get().type : null;
	}
	
	public ScriptEnvironment rootScriptEnvironment() {
		assert currentContext.get() != null : "trying to read a null context";
		
		ScriptContext root = currentContext.get().root();
		
		assert root.webSocketConnectionHost != null : "script context that is not rooted correctly";
		
		return root.webSocketConnectionHost;
	}
	
	public ScriptEnvironment scriptEnvironment() {
		switch (type()) {
		case DocumentRequest:
		case WebSocket:
		case InternalExecution:
			return webSocketConnectionHost();
		case ModuleInitialization:
			return moduleScriptEnvironment();
		}
		
		throw new AssertionError("can't make environment for " + type());
	}
	
	public ModuleScriptEnvironment moduleScriptEnvironment() {
		return currentContext.get().moduleScriptEnvironment;
	}
	
	public WebSocketConnectionHost webSocketConnectionHost() {
		return currentContext.get().webSocketConnectionHost;
	}
	
	public String baseName() {
		return scriptEnvironment().baseName();
	}
	
	RequiredModule requiredModule() {
		return currentContext.get().requiredModule;
	}
	
	public WebSocketConnection connection() {
		return currentContext.get().connection;
	}
	
	public DocumentRequestProcessor documentRequestProcessor() {
		return currentContext.get().documentRequestProcessor;
	}
	
	public HttpRequest httpRequest() {
		return currentContext.get().documentRequestProcessor.httpRequest();
	}
	
	public Document document() {
		return currentContext.get().documentRequestProcessor.document();
	}
	
	public ScriptContext initialize(final RequiredModule requiredModule, final ModuleScriptEnvironment moduleScriptEnvironment) {
		currentContext.set(new ScriptContext(currentContext.get(), requiredModule, moduleScriptEnvironment));
		return currentContext.get();
	}
	
	public ScriptContext initialize(final DocumentScriptEnvironment documentScriptEnvironment) {
		currentContext.set(new ScriptContext(currentContext.get(), documentScriptEnvironment));
		return currentContext.get();
	}
	
	public ScriptContext initialize(final WebSocketConnection connection) {
		currentContext.set(new ScriptContext(currentContext.get(), connection));
		return currentContext.get();
	}
	
	public ScriptContext initialize(final DocumentRequestProcessor documentRequestProcessor) {
		currentContext.set(new ScriptContext(currentContext.get(), documentRequestProcessor));
		return currentContext.get();
	}
	
	ScriptContext save() {
		return currentContext.get();
	}
	
	public void restore(ScriptContext scriptContext) {
		if (currentContext.get() != null) {
			throw new AssertionError(
				String.format(
					"cannot restore a context %s on top of existing context (%s)",
					scriptContext,
					currentContext.get()
				)
			);
		}
		currentContext.set(scriptContext);
	}
	
	private static final String PENDING_KEY = "pending continuation [%s]";

	private Object onBehalfOf() {
		switch (type()) {
		case DocumentRequest:
			return documentRequestProcessor();
		
		case InternalExecution:
			return webSocketConnectionHost();
			
		case ModuleInitialization:
			return moduleScriptEnvironment();
			
		case WebSocket:
			return connection();
		}
		
		throw new AssertionError();
	}

	private ContinuationPending pendingContinuation(DataStore dataStore, String key) {
		String pendingKey = String.format(PENDING_KEY, key);
		
		if (!dataStore.containsData(pendingKey)) {
			log.error("asked to retrieve a pending continuation under the wrong key {} for {}", key, onBehalfOf());
			throw new AssertionError("asked to retrieve a pending continuation under the wrong key");
		}
		
		return (ContinuationPending)dataStore.removeData(pendingKey);
	}
	
	public ContinuationPending pendingContinuation(String key) {
		
		switch(type()) {
		case DocumentRequest:
			return pendingContinuation(documentRequestProcessor(), key);
		
		case WebSocket:
			return pendingContinuation(connection(), key);
		
		case ModuleInitialization:
			return pendingContinuation(requiredModule(), key);
			
		default:
			throw new AssertionError("looking for a pending continuation in a context without ");
		}
	}
	
	public void end() {
		// if this was a connection, let it know the context ended
		if (connection() != null) {
			connection().end();
		}
		currentContext.set(currentContext.get().parent);
	}

	@Override
	public void close() {
		end();
	}
}
