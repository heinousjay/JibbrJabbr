package jj.servable;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.DateFormatHelper;
import jj.configuration.Configuration;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
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
		// if the baseName exists in the cache, we happy
		return resourceFinder.findResource(AssetResource.class, new URIMatch(httpRequest.uri()).baseName) != null;
	}
	
	private String makeAbsoluteURL(final JJHttpRequest request, final String newURI) {
		return new StringBuilder("http")
			.append(request.secure() ? "s" : "")
			.append("://")
			.append(request.host())
			.append(newURI)
			.toString();
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final HttpResponse response, 
		final HttpControl control
	) throws IOException {
		
		// this one works inline, since assets are always preloaded
		return new RequestProcessor() {
			
			@Override
			public void process() {
				
				URIMatch match = new URIMatch(request.uri());
				AssetResource asset = resourceFinder.findResource(AssetResource.class, match.baseName);
				if (match.sha == null || !match.sha.equals(asset.sha1())) {
					
					final String newURL = makeAbsoluteURL(request, asset.uri());
					
					log.debug("unqualified request for asset {}, redirecting", asset);
					
					response.status(HttpResponseStatus.TEMPORARY_REDIRECT.getCode())
						.header(HttpHeaders.Names.LOCATION, newURL)
						.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
						.end();
					
				} else {
					
					boolean notModified = false;
					
					if (request.hasHeader(HttpHeaders.Names.IF_MODIFIED_SINCE)) {
						Date ifModifiedDate = DateFormatHelper.headerDate(request.header(HttpHeaders.Names.IF_MODIFIED_SINCE));
						// might be incorrectly formatted? always possible, i guess
						if (ifModifiedDate != null && 
							!ifModifiedDate.before(asset.lastModifiedDate())) {
							
							response.status(HttpResponseStatus.NOT_MODIFIED.getCode())
								.header(HttpHeaders.Names.CACHE_CONTROL, TWENTY_YEARS)
								.header(HttpHeaders.Names.ETAG, asset.sha1())
								.header(HttpHeaders.Names.LAST_MODIFIED, asset.lastModifiedDate())
								.end();
							notModified = true;
						}
					} else if (request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH) &&
						asset.sha1().equals(request.header(HttpHeaders.Names.ETAG))) {
						
						response.status(HttpResponseStatus.NOT_MODIFIED.getCode())
							.header(HttpHeaders.Names.CACHE_CONTROL, TWENTY_YEARS)
							.header(HttpHeaders.Names.ETAG, asset.sha1())
							.header(HttpHeaders.Names.LAST_MODIFIED, asset.lastModifiedDate())
							.end();
						notModified = true;
					}
					
					if (!notModified) {
						
						response.status(HttpResponseStatus.OK.getCode())
							.header(HttpHeaders.Names.CACHE_CONTROL, TWENTY_YEARS)
							.header(HttpHeaders.Names.ETAG, asset.sha1())
							.header(HttpHeaders.Names.LAST_MODIFIED, asset.lastModifiedDate())
							.header(HttpHeaders.Names.CONTENT_LENGTH, asset.bytes().length)
							.header(HttpHeaders.Names.CONTENT_TYPE, asset.mime())
							.content(asset.bytes())
							.end();
					}
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
