package jj.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Configuration;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

@Singleton
class AssetServable extends Servable {
	
	private final Logger log = LoggerFactory.getLogger(AssetServable.class);
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	AssetServable(final Configuration configuration, final ResourceFinder resourceFinder) {
		super(configuration);
		this.resourceFinder = resourceFinder;
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
					
					response.header(HttpHeaders.Names.CONTENT_LENGTH, asset.bytes().length)
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
