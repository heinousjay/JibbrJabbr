package jj.script;

import jj.Sequence;
import jj.engine.MIME;

import com.ning.http.client.Request;

public class RestRequest {
	
	private static final Sequence sequence = new Sequence();
	
	private final Request request;
	private final String id;
	
	public RestRequest(final Request request) {
		this.request = request;
		this.id = String.format("RestRequest-%s", sequence.next());
	}

	public Request request() {
		return request;
	}
	
	public String id() {
		return id;
	}
	
	public MIME produce() {
		// for now, it's hardcoded
		return MIME.JSON;
	}
	
	public String toString() {
		return id + ": " + request;
	}
}
