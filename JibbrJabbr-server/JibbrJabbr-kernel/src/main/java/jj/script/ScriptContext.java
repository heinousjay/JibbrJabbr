package jj.script;

import jj.document.DocumentRequestProcessor;
import jj.webbit.JJWebSocketConnection;

class ScriptContext {
	
	// i think this "previous" crap is just going away...
	final ScriptContext previous;
	
	final ScriptBundle scriptBundle;
	
	final JJWebSocketConnection connection;
	
	final DocumentRequestProcessor documentRequestProcessor;
	
	ScriptContext( 
		final ScriptContext previous,
		final JJWebSocketConnection connection
	) {
		this.previous = previous;
		this.connection = connection;
		this.scriptBundle = connection.scriptBundle();
		
		this.documentRequestProcessor = null;
	}
	
	ScriptContext( 
		final ScriptContext previous,
		final DocumentRequestProcessor documentRequestProcessor
	) {
		this.previous = previous;
		this.documentRequestProcessor = documentRequestProcessor;
		this.scriptBundle = documentRequestProcessor.scriptBundle();
		
		this.connection = null;
	}
}
