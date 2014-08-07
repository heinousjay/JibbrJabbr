package jj.http.server.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.resolution.AppLocation;
import jj.css.StylesheetResource;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.http.uri.URIMatch;
import jj.resource.ResourceFinder;

// TODO! move this to the css package
@Singleton
class CssServable extends Servable<StylesheetResource> {
	
	private static final String CSS = "css";
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	CssServable(final ResourceFinder resourceFinder) {
		this.resourceFinder = resourceFinder;
	}

	@Override
	public boolean isMatchingRequest(final URIMatch uriMatch) {
		return CSS.equals(uriMatch.extension);
	}
	
	@Override
	public StylesheetResource loadResource(final URIMatch match) {
		return resourceFinder.loadResource(StylesheetResource.class, AppLocation.Base, match.baseName);
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpServerRequest request,
		final HttpServerResponse response
	) throws IOException {
		
		StylesheetResource resource = loadResource(request.uriMatch());
		RequestProcessor result = null;
		if (resource != null) {
			result = makeStandardRequestProcessor(request, response, resource);
		}
		return result;
	}
}
