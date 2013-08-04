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
class AssetServable extends Servable<AssetResource> {
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	AssetServable(final Configuration configuration, final ResourceFinder resourceFinder) {
		super(configuration);
		this.resourceFinder = resourceFinder;
	}
	
	@Override
	public boolean isMatchingRequest(final URIMatch match) {
		return resourceFinder.findResource(AssetResource.class, match.baseName) != null;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		
		final URIMatch match = request.uriMatch();
		final AssetResource asset = loadResource(match);
		return asset == null ? null : makeStandardRequestProcessor(request, response, match, asset);
	}

	@Override
	public AssetResource loadResource(URIMatch match) {
		return resourceFinder.findResource(AssetResource.class, match.baseName);
	}

	@Override
	public Class<AssetResource> type() {
		return AssetResource.class;
	}

}
