package jj.webbit;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.AssetResource;
import jj.resource.ResourceFinder;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

@Singleton
public class NotFoundHttpHandler implements HttpHandler {
	
	private static final String NOT_FOUND = "errors/404.html";
	
	private final ResourceFinder finder;
	
	@Inject
	NotFoundHttpHandler(final ResourceFinder finder) {
		this.finder = finder;
	}
	
	@Override
	public void handleHttpRequest(
		final HttpRequest request,
		final HttpResponse response,
		final HttpControl control
	) throws Exception {
		// this can be overriden later on, perhaps? of course!
		// but that will be handled in the engine handler. at
		// some point the static file handler has to go away
		// and we do all serving duties.
		AssetResource notFound = finder.findResource(AssetResource.class, NOT_FOUND);
		if (notFound == null) { // the irony!
			response.error(new IllegalStateException("internal assets are missing!"));
		} else {
			response.status(HttpResponseStatus.NOT_FOUND.getCode())
				.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
				.header(HttpHeaders.Names.CONTENT_TYPE, notFound.mime())
				.header(HttpHeaders.Names.CONTENT_LENGTH, notFound.bytes().limit())
				.content(notFound.bytes())
				.end();
		}
	}

}
