package jj.script;

import java.io.Serializable;

import jj.continuation.RestRequest;
import jj.jqmessage.JQueryMessage;

public class ContinuationState implements Serializable {
	
	/**
	 * simple helper to produce a correctly typed result
	 * post continuation.  you should define a toString
	 * method that describes the return value for debugging
	 * @author jason
	 *
	 */
	public interface Returns {
		
		Object transform(String value);
	}
	
	private final Returns returns;

	private final Object message;
	
	private final ContinuationType type;
	
	public ContinuationState(final JQueryMessage jQueryMessage, final Returns returns) {
		this.type = ContinuationType.JQueryMessage; 
		this.message = jQueryMessage;
		this.returns = returns;
	}
	
	public ContinuationState(final RestRequest request, final Returns returns) {
		this.type = ContinuationType.AsyncHttpRequest;
		this.message = request;
		this.returns = returns;
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
	
	public String pendingKey() {
		switch (type) {
		case AsyncHttpRequest:
			return restRequest().id();
		case JQueryMessage:
			return jQueryMessage().id();
		case Require:
			return null;
		}
		
		throw new AssertionError("weird construction, can't happen");
	}
	
	public Object produceReturn(String value) {
		return returns.transform(value);
	}
	
	public String toString() {
		return new StringBuilder("type: ")
			.append(type())
			.append(", message: ")
			.append(message)
			.append(", returns: ")
			.append(returns)
			.toString();
	}
}
