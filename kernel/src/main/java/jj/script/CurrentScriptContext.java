package jj.script;

import java.io.Closeable;

import javax.inject.Singleton;

import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
import jj.http.server.WebSocketConnection;
import jj.http.server.WebSocketConnectionHost;
import jj.http.server.servable.document.DocumentRequestProcessor;

/**
 * Maintains the current context for a thread
 * executing a script.  basically a multimode
 * repository of information 
 * @author jason
 *
 */
@Singleton
public class CurrentScriptContext implements Closeable {
	
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
	
	private WebSocketConnection connection() {
		return currentContext.get().connection;
	}
	
	public DocumentRequestProcessor documentRequestProcessor() {
		return currentContext.get().documentRequestProcessor;
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
