package jj.http.server.servable;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.uri.URIMatch;
import jj.http.HttpRequest;
import jj.http.HttpResponse;


@Singleton
class AssetServable extends Servable {
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	AssetServable(final Configuration configuration, final ResourceFinder resourceFinder) {
		super(configuration);
		this.resourceFinder = resourceFinder;
	}
	
	@Override
	public boolean isMatchingRequest(HttpRequest httpRequest) {
		// if the baseName exists in the cache, we happy
		URIMatch match = new URIMatch(httpRequest.uri());
		
		return resourceFinder.findResource(AssetResource.class, match.baseName) != null;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		
		final URIMatch match = new URIMatch(request.uri());
		final AssetResource asset = resourceFinder.findResource(AssetResource.class, match.baseName);

		return asset == null ? null : makeStandardRequestProcessor(request, response, match, asset);
	}

}
