package jj.script;

import static jj.script.ScriptContextType.*;

import jj.http.server.JJWebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;

class ScriptContext {
	
	final ScriptContextType type;
	
	final ScriptContext parent;
	
	final DocumentScriptExecutionEnvironment associatedScriptExecutionEnvironment;
	
	final ModuleScriptExecutionEnvironment moduleScriptExecutionEnvironment;
	
	final RequiredModule requiredModule;
	
	final JJWebSocketConnection connection;
	
	final DocumentRequestProcessor documentRequestProcessor;
	
	ScriptContext(
		final ScriptContext parent,
		final RequiredModule requiredModule,
		final ModuleScriptExecutionEnvironment moduleScriptExecutionEnvironment
	) {
		this.type = ModuleInitialization;
		this.parent = parent;
		this.requiredModule = requiredModule;
		this.moduleScriptExecutionEnvironment = moduleScriptExecutionEnvironment;

		this.associatedScriptExecutionEnvironment = null;
		this.connection = null;
		this.documentRequestProcessor = null;
	}
	
	ScriptContext(
		final ScriptContext parent,
		final DocumentScriptExecutionEnvironment associatedScriptExecutionEnvironment
	) {
		this.type = InternalExecution;
		this.parent = parent;
		this.associatedScriptExecutionEnvironment = associatedScriptExecutionEnvironment;

		this.connection = null;
		this.documentRequestProcessor = null;
		this.moduleScriptExecutionEnvironment = null;
		this.requiredModule = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final JJWebSocketConnection connection
	) {
		this.type = WebSocket;
		this.parent = parent;
		this.connection = connection;
		this.associatedScriptExecutionEnvironment = connection.associatedScriptExecutionEnvironment();
		
		this.documentRequestProcessor = null;
		this.moduleScriptExecutionEnvironment = null;
		this.requiredModule = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final DocumentRequestProcessor documentRequestProcessor
	) {
		this.type = DocumentRequest;
		this.parent = parent;
		this.documentRequestProcessor = documentRequestProcessor;
		this.associatedScriptExecutionEnvironment = documentRequestProcessor.associatedScriptExecutionEnvironment();
		
		this.connection = null;
		this.moduleScriptExecutionEnvironment = null;
		this.requiredModule = null;
	}
}
