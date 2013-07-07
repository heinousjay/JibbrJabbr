package jj.http;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.AssetResource;
import jj.resource.ResourceFinder;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

@Singleton
public class NotFoundHttpHandler {
	
	private static final String NOT_FOUND = "errors/404.html";
	
	private final ResourceFinder finder;
	
	@Inject
	NotFoundHttpHandler(final ResourceFinder finder) {
		this.finder = finder;
	}
	
	public void handleHttpRequest(
		final HttpRequest request,
		final HttpResponse response
	) throws Exception {
		// this can be overriden later on, perhaps? of course!
		// but that will be handled in the engine handler. at
		// some point the static file handler has to go away
		// and we do all serving duties.
		AssetResource notFound = finder.findResource(AssetResource.class, NOT_FOUND);
		if (notFound == null) { // the irony!
			response.error(new IllegalStateException("internal assets are missing!"));
		} else {
			response.status(HttpResponseStatus.NOT_FOUND)
				.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
				.header(HttpHeaders.Names.CONTENT_TYPE, notFound.mime())
				.header(HttpHeaders.Names.CONTENT_LENGTH, notFound.bytes().readableBytes())
				.content(notFound.bytes())
				.end();
		}
	}

}
