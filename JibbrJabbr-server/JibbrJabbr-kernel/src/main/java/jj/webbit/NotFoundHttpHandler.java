package jj.webbit;

import static java.nio.charset.StandardCharsets.UTF_8;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

class NotFoundHttpHandler implements HttpHandler {
	
	private static final String NOT_FOUND = "errors/404.html";
	
	private final ResourceFinder finder;

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
			response.charset(UTF_8)
				.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
				.header(HttpHeaders.Names.CONTENT_TYPE, notFound.mime())
				.content(notFound.bytes())
				.status(404)
				.end();
		}
	}

}
