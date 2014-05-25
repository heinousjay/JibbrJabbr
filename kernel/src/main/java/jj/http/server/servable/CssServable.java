package jj.http.server.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.resolution.AppLocation;
import jj.configuration.resolution.Application;
import jj.css.CssResource;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.resource.ResourceFinder;
import jj.uri.URIMatch;

// TODO! move this to the css package
@Singleton
class CssServable extends Servable<CssResource> {
	
	private static final String CSS = "css";
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	CssServable(
		final Application app,
		final ResourceFinder resourceFinder
	) {
		super(app);
		this.resourceFinder = resourceFinder;
	}

	@Override
	public boolean isMatchingRequest(final URIMatch uriMatch) {
		return CSS.equals(uriMatch.extension);
	}
	
	@Override
	public CssResource loadResource(final URIMatch match) {
		CssResource resource = resourceFinder.loadResource(CssResource.class, AppLocation.Base, match.baseName, true);
		if (resource == null) {
			resource = resourceFinder.loadResource(CssResource.class, AppLocation.Base, match.baseName);
		}
		
		return resource;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpServerRequest request,
		final HttpServerResponse response
	) throws IOException {
		
		final URIMatch match = request.uriMatch();
		
		// try less first
		CssResource resource = loadResource(match);
		
		RequestProcessor result = null;
		if (resource != null && isServableResource(resource)) {
			result = makeStandardRequestProcessor(request, response, match, resource);
		}
		return result;
	}
}
