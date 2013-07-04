package jj.servable;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.resource.URIMatch;
import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
import jj.http.RequestProcessor;

import io.netty.handler.codec.http.HttpHeaders;

@Singleton
class AssetServable extends Servable {
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	AssetServable(final Configuration configuration, final ResourceFinder resourceFinder) {
		super(configuration);
		this.resourceFinder = resourceFinder;
	}
	
	@Override
	public boolean isMatchingRequest(JJHttpRequest httpRequest) {
		// if the baseName exists in the cache, we happy
		URIMatch match = new URIMatch(httpRequest.uri());
		
		return resourceFinder.findResource(AssetResource.class, match.baseName) != null;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final JJHttpResponse response
	) throws IOException {
		
		// this one works inline, since assets are always preloaded
		return new RequestProcessor() {
			
			@Override
			public void process() {
				
				URIMatch match = new URIMatch(request.uri());
				AssetResource asset = resourceFinder.findResource(AssetResource.class, match.baseName);
				
				if (request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH) &&
					asset.sha1().equals(request.header(HttpHeaders.Names.IF_NONE_MATCH))) {
					
					response.sendNotModified(asset, match.versioned);

				} else if (match.sha == null) {
					
					if (match.versioned) {
						response.sendCachedResource(asset);
					} else {
						response.sendUncachedResource(asset);
					}
					
				} else if (!match.sha.equals(asset.sha1())) {
				
					response.sendTemporaryRedirect(asset);
					
				} else {
					
					response.sendCachedResource(asset);
				}
			}
		};
	}

}
