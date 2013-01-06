package jj.script;

import jj.document.DocumentRequestProcessor;
import jj.webbit.JJWebSocketConnection;

class ScriptContext {
	
	final ScriptContext parent;
	
	final AssociatedScriptBundle associatedScriptBundle;
	
	final ModuleScriptBundle moduleScriptBundle;
	
	final JJWebSocketConnection connection;
	
	final DocumentRequestProcessor documentRequestProcessor;
	
	ScriptContext(
		final ScriptContext parent,
		final ModuleScriptBundle moduleScriptBundle
	) {
		this.parent = parent;
		this.moduleScriptBundle = moduleScriptBundle;

		this.associatedScriptBundle = null;
		this.connection = null;
		this.documentRequestProcessor = null;
	}
	
	ScriptContext(
		final ScriptContext parent,
		final AssociatedScriptBundle associatedScriptBundle
	) {
		this.parent = parent;
		this.associatedScriptBundle = associatedScriptBundle;

		this.connection = null;
		this.documentRequestProcessor = null;
		this.moduleScriptBundle = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final JJWebSocketConnection connection
	) {
		this.parent = parent;
		this.connection = connection;
		this.associatedScriptBundle = connection.associatedScriptBundle();
		
		this.documentRequestProcessor = null;
		this.moduleScriptBundle = null;
	}
	
	ScriptContext( 
		final ScriptContext parent,
		final DocumentRequestProcessor documentRequestProcessor
	) {
		this.parent = parent;
		this.documentRequestProcessor = documentRequestProcessor;
		this.associatedScriptBundle = documentRequestProcessor.associatedScriptBundle();
		
		this.connection = null;
		this.moduleScriptBundle = null;
	}
}
