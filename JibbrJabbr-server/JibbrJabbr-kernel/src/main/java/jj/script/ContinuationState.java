package jj.script;

import java.io.Serializable;

import jj.jqmessage.JQueryMessage;

public class ContinuationState implements Serializable {
	
	private final Object message;
	
	private final ContinuationType type;
	
	public ContinuationState(final JQueryMessage jQueryMessage) {
		this.type = ContinuationType.JQueryMessage; 
		this.message = jQueryMessage;
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
	
	public JQueryMessage jQueryMessage() {
		return type == ContinuationType.JQueryMessage ? (JQueryMessage)message : null;
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
			return restRequest().id();
		case JQueryMessage:
			return jQueryMessage().id();
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
