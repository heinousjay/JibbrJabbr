package jj.script;

import jj.document.DocumentRequestProcessor;
import jj.webbit.JJWebSocketConnection;

class ScriptContext {
	
	final ScriptContext previous;
	
	final AssociatedScriptBundle associatedScriptBundle;
	
	final JJWebSocketConnection connection;
	
	final DocumentRequestProcessor documentRequestProcessor;
	
	ScriptContext(
		final ScriptContext previous,
		final AssociatedScriptBundle associatedScriptBundle
	) {
		this.previous = previous;
		this.associatedScriptBundle = associatedScriptBundle;

		this.connection = null;
		this.documentRequestProcessor = null;
	}
	
	ScriptContext( 
		final ScriptContext previous,
		final JJWebSocketConnection connection
	) {
		this.previous = previous;
		this.connection = connection;
		this.associatedScriptBundle = connection.associatedScriptBundle();
		
		this.documentRequestProcessor = null;
	}
	
	ScriptContext( 
		final ScriptContext previous,
		final DocumentRequestProcessor documentRequestProcessor
	) {
		this.previous = previous;
		this.documentRequestProcessor = documentRequestProcessor;
		this.associatedScriptBundle = documentRequestProcessor.associatedScriptBundle();
		
		this.connection = null;
	}
}
