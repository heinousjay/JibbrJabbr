package jj.script;

import java.io.Serializable;

import jj.jjmessage.JJMessage;

public class ContinuationState implements Serializable {
	
	private final Object message;
	
	private final ContinuationType type;
	
	public ContinuationState(final JJMessage jjMessage) {
		this.type = ContinuationType.JJMessage; 
		this.message = jjMessage;
	}
	
	public ContinuationState(final RestRequest request) {
		this.type = ContinuationType.AsyncHttpRequest;
		this.message = request;
	}
	
	public ContinuationState(final RequiredModule require) {
		this.type = ContinuationType.RequiredModule;
		this.message = require;
	}

	private static final long serialVersionUID = 1L;
	
	public ContinuationType type() {
		return type;
	}
	
	public JJMessage jjMessage() {
		return type == ContinuationType.JJMessage ? (JJMessage)message : null;
	}
	
	public RestRequest restRequest() {
		return type == ContinuationType.AsyncHttpRequest ? (RestRequest)message : null;
	}
	
	public RequiredModule requiredModule() {
		return type == ContinuationType.RequiredModule ? (RequiredModule)message : null;
	}
	
	public String pendingKey() {
		switch (type) {
		case AsyncHttpRequest:
			return restRequest().pendingKey();
		case JJMessage:
			return jjMessage().pendingKey();
		case RequiredModule:
			return requiredModule().pendingKey();
		}
		
		throw new AssertionError("weird construction, can't happen");
	}
	
	public String toString() {
		return new StringBuilder("type: ")
			.append(type())
			.append(", message: ")
			.append(message)
			.toString();
	}
}
