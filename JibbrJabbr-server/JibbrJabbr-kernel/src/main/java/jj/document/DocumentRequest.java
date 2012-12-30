package jj.document;

import jj.resource.Resource;
import jj.webbit.JJHttpRequest;

import org.jsoup.nodes.Document;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

/**
 * All of the information associated with a document request,
 * and a flag to indicate if the ready function has completed
 * processing yet, to assist with continuation processing
 * @author jason
 *
 */
public class DocumentRequest {
	private final String baseName;
	private final String mime;
	private final Document document;
	private final JJHttpRequest httpRequest;
	private final HttpResponse httpResponse;
	private final HttpControl httpControl;
	
	public DocumentRequest(
		final Resource resource,
		final Document document,
		final JJHttpRequest httpRequest,
		final HttpResponse httpResponse,
		final HttpControl httpControl	
	) {
		this.baseName = resource.baseName();
		this.mime = resource.mime();
		this.document = document.clone();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.httpControl = httpControl;
	}
	
	public String mime() {
		return mime;
	}
	
	public String baseName() {
		return baseName;
	}
	
	public Document document() {
		return document;
	}
	
	public JJHttpRequest httpRequest() {
		return httpRequest;
	}
	
	public HttpResponse httpResponse() {
		return httpResponse;
	}
	
	public HttpControl httpControl() {
		return httpControl;
	}
}
