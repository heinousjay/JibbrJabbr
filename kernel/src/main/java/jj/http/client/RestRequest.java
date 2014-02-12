package jj.http.client;

import jj.engine.MIME;
import jj.script.Continuation;
import jj.script.ContinuationPendingKey;

public class RestRequest implements Continuation {
	
	private final JJHttpClientRequest request;
	private ContinuationPendingKey pendingKey;
	
	public RestRequest(final JJHttpClientRequest request) {
		this.request = request;
	}

	public JJHttpClientRequest request() {
		return request;
	}
	
	@Override
	public ContinuationPendingKey pendingKey() {
		return pendingKey;
	}
	
	@Override
	public void pendingKey(ContinuationPendingKey pendingKey) {
		this.pendingKey = pendingKey;
	}
	
	public MIME produce() {
		// for now, it's hardcoded
		return MIME.JSON;
	}
	
	public String toString() {
		return pendingKey + ": " + request;
	}
}
