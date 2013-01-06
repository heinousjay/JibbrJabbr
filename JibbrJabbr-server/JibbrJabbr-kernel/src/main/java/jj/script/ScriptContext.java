package jj.script;

import jj.document.DocumentRequestProcessor;
import jj.webbit.JJWebSocketConnection;

class ScriptContext {
	
	final ScriptContext previous;
	
	final AssociatedScriptBundle associatedScriptBundle;
	
	final ModuleScriptBundle moduleScriptBundle;
	
	final JJWebSocketConnection connection;
	
	final DocumentRequestProcessor documentRequestProcessor;
	
	ScriptContext(
		final ScriptContext previous,
		final ModuleScriptBundle moduleScriptBundle
	) {
		this.previous = previous;
		this.moduleScriptBundle = moduleScriptBundle;

		this.associatedScriptBundle = null;
		this.connection = null;
		this.documentRequestProcessor = null;
	}
	
	ScriptContext(
		final ScriptContext previous,
		final AssociatedScriptBundle associatedScriptBundle
	) {
		this.previous = previous;
		this.associatedScriptBundle = associatedScriptBundle;

		this.connection = null;
		this.documentRequestProcessor = null;
		this.moduleScriptBundle = null;
	}
	
	ScriptContext( 
		final ScriptContext previous,
		final JJWebSocketConnection connection
	) {
		this.previous = previous;
		this.connection = connection;
		this.associatedScriptBundle = connection.associatedScriptBundle();
		
		this.documentRequestProcessor = null;
		this.moduleScriptBundle = null;
	}
	
	ScriptContext( 
		final ScriptContext previous,
		final DocumentRequestProcessor documentRequestProcessor
	) {
		this.previous = previous;
		this.documentRequestProcessor = documentRequestProcessor;
		this.associatedScriptBundle = documentRequestProcessor.associatedScriptBundle();
		
		this.connection = null;
		this.moduleScriptBundle = null;
	}
}
