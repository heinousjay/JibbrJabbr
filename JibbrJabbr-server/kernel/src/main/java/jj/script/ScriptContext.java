package jj.script;

import static jj.script.ScriptContextType.*;

import jj.http.server.JJWebSocketConnection;
import jj.http.server.WebSocketConnectionHost;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;

class ScriptContext {
	
	final ScriptContextType type;
	
	final ScriptContext parent;
	
	final WebSocketConnectionHost webSocketConnectionHost;
	
	final ModuleScriptEnvironment moduleScriptEnvironment;
	
	final RequiredModule requiredModule;
	
	final JJWebSocketConnection connection;
	
	final DocumentRequestProcessor documentRequestProcessor;
	
	ScriptContext(
		final ScriptContext parent,
		final RequiredModule requiredModule,
		final ModuleScriptEnvironment moduleScriptEnvironment
	) {
		this.type = ModuleInitialization;
		this.parent = parent;
		this.requiredModule = requiredModule;
		this.moduleScriptEnvironment = moduleScriptEnvironment;

		this.webSocketConnectionHost = null;
		this.connection = null;
		this.documentRequestProcessor = null;
	}
	
	ScriptContext(
		final ScriptContext parent,
		final DocumentScriptEnvironment documentScriptEnvironment
	) {
		this.type = InternalExecution;
		this.parent = parent;
		this.webSocketConnectionHost = documentScriptEnvironment;

		this.connection = null;
		this.documentRequestProcessor = null;
		this.moduleScriptEnvironment = null;
		this.requiredModule = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final JJWebSocketConnection connection
	) {
		this.type = WebSocket;
		this.parent = parent;
		this.connection = connection;
		this.webSocketConnectionHost = connection.webSocketConnectionHost();
		
		this.documentRequestProcessor = null;
		this.moduleScriptEnvironment = null;
		this.requiredModule = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final DocumentRequestProcessor documentRequestProcessor
	) {
		this.type = DocumentRequest;
		this.parent = parent;
		this.documentRequestProcessor = documentRequestProcessor;
		this.webSocketConnectionHost = documentRequestProcessor.documentScriptEnvironment();
		
		this.connection = null;
		this.moduleScriptEnvironment = null;
		this.requiredModule = null;
	}
	
	ScriptContext root() {
		ScriptContext result = this;
		while (result.parent != null) result = result.parent;
		return result;
	}
	
	@Override
	public String toString() {
		return type.toString();
	}
}
