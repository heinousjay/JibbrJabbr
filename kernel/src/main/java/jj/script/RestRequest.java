package jj.script;

import jj.engine.MIME;
import jj.http.client.JJHttpClientRequest;

public class RestRequest implements Continuable {
	
	private final JJHttpClientRequest request;
	private String pendingKey;
	
	public RestRequest(final JJHttpClientRequest request) {
		this.request = request;
	}

	public JJHttpClientRequest request() {
		return request;
	}
	
	@Override
	public String pendingKey() {
		return pendingKey;
	}
	
	@Override
	public void pendingKey(String pendingKey) {
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
