package jj.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
import jj.http.RequestProcessor;
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
	public boolean isMatchingRequest(final JJHttpRequest request) {
		// match everything that ends in .css
		return request.uri().endsWith(CSS);
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final JJHttpResponse response
	) throws IOException {
		final URIMatch match = new URIMatch(request.uri());
		CssResource resource = resourceFinder.loadResource(CssResource.class, match.baseName, true);
		if (resource == null) {
			resource = resourceFinder.loadResource(CssResource.class, match.baseName);
		}
		
		if (resource == null) {
			return null;
		} else if (!isServablePath(resource.path())) {
			// TODO log this.  and really make this logic
			// centralized somehow
			return null;
		}
		
		final CssResource css = resource;
		
		return new RequestProcessor() {
			
			@Override
			public void process() throws IOException {
				doStandardResponse(request, response, match, css);
			}
		};
	}
}
