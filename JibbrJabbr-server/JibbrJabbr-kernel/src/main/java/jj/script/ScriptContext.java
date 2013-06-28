package jj.script;

import static jj.script.ScriptContextType.*;

import jj.document.DocumentRequestProcessor;
import jj.http.JJWebSocketConnection;

class ScriptContext {
	
	final ScriptContextType type;
	
	final ScriptContext parent;
	
	final AssociatedScriptBundle associatedScriptBundle;
	
	final ModuleScriptBundle moduleScriptBundle;
	
	final RequiredModule requiredModule;
	
	final JJWebSocketConnection connection;
	
	final DocumentRequestProcessor documentRequestProcessor;
	
	ScriptContext(
		final ScriptContext parent,
		final ModuleScriptBundle moduleScriptBundle,
		final RequiredModule requiredModule
	) {
		this.type = ModuleInitialization;
		this.parent = parent;
		this.moduleScriptBundle = moduleScriptBundle;
		this.requiredModule = requiredModule;

		this.associatedScriptBundle = null;
		this.connection = null;
		this.documentRequestProcessor = null;
	}
	
	ScriptContext(
		final ScriptContext parent,
		final AssociatedScriptBundle associatedScriptBundle
	) {
		this.type = InternalExecution;
		this.parent = parent;
		this.associatedScriptBundle = associatedScriptBundle;

		this.connection = null;
		this.documentRequestProcessor = null;
		this.moduleScriptBundle = null;
		this.requiredModule = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final JJWebSocketConnection connection
	) {
		this.type = WebSocket;
		this.parent = parent;
		this.connection = connection;
		this.associatedScriptBundle = connection.associatedScriptBundle();
		
		this.documentRequestProcessor = null;
		this.moduleScriptBundle = null;
		this.requiredModule = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final DocumentRequestProcessor documentRequestProcessor
	) {
		this.type = HttpRequest;
		this.parent = parent;
		this.documentRequestProcessor = documentRequestProcessor;
		this.associatedScriptBundle = documentRequestProcessor.associatedScriptBundle();
		
		this.connection = null;
		this.moduleScriptBundle = null;
		this.requiredModule = null;
	}
}
