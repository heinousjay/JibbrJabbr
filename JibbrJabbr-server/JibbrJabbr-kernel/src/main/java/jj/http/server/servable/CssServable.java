package jj.http.server.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.resource.CssResource;
import jj.resource.ResourceFinder;
import jj.uri.URIMatch;

@Singleton
class CssServable extends Servable {
	
	private static final String CSS = ".css";
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	CssServable(
		final Configuration configuration,
		final ResourceFinder resourceFinder
	) {
		super(configuration);
		this.resourceFinder = resourceFinder;
	}

	@Override
	public boolean isMatchingRequest(final HttpRequest request) {
		// match everything that ends in .css
		return request.uri().endsWith(CSS);
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		
		final URIMatch match = new URIMatch(request.uri());
		
		// try less first
		CssResource resource = resourceFinder.loadResource(CssResource.class, match.baseName, true);
		if (resource == null) {
			resource = resourceFinder.loadResource(CssResource.class, match.baseName);
		}
		
		RequestProcessor result = null;
		if (resource != null && isServablePath(resource.path())) {
			result = makeStandardRequestProcessor(request, response, match, resource);
		}
		return result;
	}
}
