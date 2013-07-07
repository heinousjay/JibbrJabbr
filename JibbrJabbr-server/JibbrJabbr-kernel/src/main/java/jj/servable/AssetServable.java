package jj.servable;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.uri.URIMatch;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.RequestProcessor;


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
		
		// this one works inline, since assets are always preloaded
		return new RequestProcessor() {
			
			@Override
			public void process() {
				
				URIMatch match = new URIMatch(request.uri());
				AssetResource asset = resourceFinder.findResource(AssetResource.class, match.baseName);
				
				doStandardResponse(request, response, match, asset);
			}
		};
	}

}
