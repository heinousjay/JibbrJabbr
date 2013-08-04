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
class CssServable extends Servable<CssResource> {
	
	private static final String CSS = "css";
	
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
	public boolean isMatchingRequest(final URIMatch uriMatch) {
		return CSS.equals(uriMatch.extension);
	}
	
	@Override
	public CssResource loadResource(final URIMatch match) {
		CssResource resource = resourceFinder.loadResource(CssResource.class, match.baseName, true);
		if (resource == null) {
			resource = resourceFinder.loadResource(CssResource.class, match.baseName);
		}
		
		return resource;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		
		final URIMatch match = request.uriMatch();
		
		// try less first
		CssResource resource = loadResource(match);
		
		RequestProcessor result = null;
		if (resource != null && isServablePath(resource.path())) {
			result = makeStandardRequestProcessor(request, response, match, resource);
		}
		return result;
	}

	@Override
	public Class<CssResource> type() {
		return CssResource.class;
	}
}
