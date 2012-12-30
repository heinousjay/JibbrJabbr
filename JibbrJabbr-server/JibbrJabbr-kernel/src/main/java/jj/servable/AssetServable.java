package jj.servable;

import java.io.IOException;
import java.nio.file.Path;

import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

class AssetServable extends Servable {
	
	private final Logger log = LoggerFactory.getLogger(AssetServable.class);
	
	@SuppressWarnings("unused")
	private static final String TWENTY_YEARS = String.valueOf(60 * 60 * 24 * 365 * 20);
	
	private final ResourceFinder resourceFinder;

	AssetServable(final Path basePath, final ResourceFinder resourceFinder) {
		super(basePath);
		this.resourceFinder = resourceFinder;
	}
	
	@Override
	protected Rank rank() {
		// we happen after everyone else goes, to give
		// a chance at overrides
		return Rank.Last;
	}
	
	private String baseName(JJHttpRequest httpRequest) {
		return httpRequest.uri().substring(1);
	}

	@Override
	public boolean isMatchingRequest(JJHttpRequest httpRequest) {		
		return resourceFinder.findResource(AssetResource.class, baseName(httpRequest)) != null;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final HttpResponse response, 
		final HttpControl control
	) throws IOException {
		// this one works inline
		return new RequestProcessor() {
			
			@Override
			public void process() {
				
				
				try {
					AssetResource asset = resourceFinder.findResource(AssetResource.class, baseName(request));
					
					response
						// once the URL rewriting works, then:
						// HttpHeaders.Values.MAX_AGE + "=" + TWENTY_YEARS)
						.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
						.header(
							HttpHeaders.Names.CONTENT_TYPE, 
							asset.mime()
						)
						.content(asset.bytes())
						.end();
					
				} catch (Exception e) {
					log.error("error responding to {}", request.uri());
					throw e;
				}
				
				log.info(
					"request for [{}] completed in {} milliseconds (wall time)",
					request.uri(),
					request.wallTime()
				);
			}
		};
	}

}
