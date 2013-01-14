package jj.document;

import jj.Sequence;
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
	
	private static final Sequence idSequence = new Sequence();
	
	private final String baseName;
	private final String mime;
	private final Document document;
	private final JJHttpRequest httpRequest;
	private final HttpResponse httpResponse;
	private final HttpControl httpControl;
	private final String id = "request " + idSequence.next();
	
	// this is a little hacky but the idea is if the request needed IO, it's the first time
	// through, so the ResourceUrlDocumentFilter will also need IO to warm up the cache
	// with resources.  this may behave strangely in development if you do some file deletions
	// and stuff... but the (eventual) REPL will eventually have a command to flush caches and
	// force loading if things aren't getting picked up.
	private final boolean neededIO;
	
	public DocumentRequest(
		final Resource resource,
		final Document document,
		final JJHttpRequest httpRequest,
		final HttpResponse httpResponse,
		final HttpControl httpControl,
		final boolean neededIO
	) {
		this.baseName = resource.baseName();
		this.mime = resource.mime();
		this.document = document.clone();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.httpControl = httpControl;
		this.neededIO = neededIO;
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
	
	public String id() {
		return id;
	}
	
	public boolean neededIO() {
		return neededIO;
	}
	
	public String toString() {
		return DocumentRequest.class.getSimpleName() + " for " + baseName + " (" + id + ")  on connection " + httpRequest;
	}
}
