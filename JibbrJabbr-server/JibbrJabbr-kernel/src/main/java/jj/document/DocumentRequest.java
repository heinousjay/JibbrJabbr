package jj.document;

import jj.resource.HtmlResource;
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
	private final HtmlResource htmlResource;
	private final Document document;
	private final JJHttpRequest httpRequest;
	private final HttpResponse httpResponse;
	private final HttpControl httpControl;
	
	public DocumentRequest(
		final HtmlResource htmlResource,
		final JJHttpRequest httpRequest,
		final HttpResponse httpResponse,
		final HttpControl httpControl	
	) {
		this.htmlResource = htmlResource;
		this.document = this.htmlResource.document().clone();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.httpControl = httpControl;
	}
	
	public HtmlResource htmlResource() {
		return htmlResource;
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
