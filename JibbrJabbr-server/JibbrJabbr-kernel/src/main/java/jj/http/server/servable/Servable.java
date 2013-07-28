package jj.http.server.servable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import jj.configuration.Configuration;
import jj.execution.IOThread;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.resource.Resource;
import jj.uri.URIMatch;

import io.netty.handler.codec.http.HttpHeaders;

public abstract class Servable {
	
	/**
	 * twenty years in seconds for cache control
	 */
	protected static final String TWENTY_YEARS = HttpHeaders.Values.MAX_AGE + "=" + String.valueOf(60 * 60 * 24 * 365 * 20);
	
	protected final Path basePath;
	
	protected Servable(final Configuration configuration) {
		this.basePath = configuration.basePath();
	}
	
	/**
	 * ensures that the path we end up with is real and under our base path. some
	 * servables care, some don't
	 * @param result
	 * @return
	 */
	@IOThread
	protected boolean isServablePath(final Path result) {
		return Files.exists(result, LinkOption.NOFOLLOW_LINKS) && result.startsWith(basePath);
	}
	
	/**
	 * Indicates if the incoming request can potentially be served by this servable.
	 * this is more or less a first opportunity for trivial rejections (like extensions
	 * you aren't interested in, or the lack of matching resources in the cache.. whatever
	 * really).  returning true from this method does not hold this servable to produce a 
	 * request processor, it's possible to return null from that method to pass up on
	 * serving the request
	 * @param httpRequest
	 * @return
	 */
	public abstract boolean isMatchingRequest(final HttpRequest httpRequest);
	
	public abstract RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException;

	/**
	 * Produces a standard response for a resource, handling caching, validation, and
	 * efficient transfer of the bytes, and erroring out on error
	 * @param request
	 * @param response
	 * @param match
	 * @param resource
	 */
	protected void doStandardResponse(final HttpRequest request, final HttpResponse response, URIMatch match, Resource resource) {
		try {
			if (request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH) &&
				resource.sha1().equals(request.header(HttpHeaders.Names.IF_NONE_MATCH))) {
				
				response.sendNotModified(resource, match.versioned);
	
			} else if (match.versioned) {
				
				response.sendCachedResource(resource);
				
			} else if (match.sha == null) {
				
				response.sendUncachedResource(resource);
				
			} else if (!match.sha.equals(resource.sha1())) {
			
				response.sendTemporaryRedirect(resource);
				
			} else {
				
				response.sendCachedResource(resource);
			}
		} catch (Exception e) {
			response.error(e);
		}
	}

}
