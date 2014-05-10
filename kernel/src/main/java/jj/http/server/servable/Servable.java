package jj.http.server.servable;


import static jj.configuration.resolution.AppLocation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import jj.configuration.resolution.Application;
import jj.http.server.HttpRequest;
import jj.http.server.HttpResponse;
import jj.resource.FileResource;
import jj.resource.ResourceThread;
import jj.resource.Resource;
import jj.uri.URIMatch;
import io.netty.handler.codec.http.HttpHeaders;

public abstract class Servable<T extends Resource> {
	
	protected final Application app;
	
	protected Servable(final Application app) {
		this.app = app;
	}
	
	// TODO probably removing this?
	protected Path appPath() {
		return app.path();
	}
	
	/**
	 * ensures that the path we end up with is real and under our base path. some
	 * servables care, some don't
	 * @param result
	 * @return
	 */
	@ResourceThread
	protected boolean isServableResource(final FileResource resource) {
		boolean result = (resource.base() == Assets || resource.base() == Virtual);
		if (result == false) {
			final Path normalized = resource.path().normalize();
			result = Files.exists(normalized, LinkOption.NOFOLLOW_LINKS) && normalized.startsWith(appPath());
		}
		return result;
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
	public abstract boolean isMatchingRequest(final URIMatch uriMatch);
	
	@ResourceThread
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
	protected RequestProcessor makeStandardRequestProcessor(
		final HttpRequest request,
		final HttpResponse response,
		final URIMatch match,
		final Resource resource
	) {
		return new RequestProcessor() {

			@Override
			public void process() throws IOException {
				try {
					
					// if the e-tag matches our SHA, 304
					if (request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH) &&
						resource.sha1().equals(request.header(HttpHeaders.Names.IF_NONE_MATCH))) {
						
						response.sendNotModified(resource, match.versioned);
					
					// if the URI was versioned, we send a cacheable resource if
					// there was no SHA in the URL, or the SHA matches the resource
					} else if (
						match.versioned && 
						(match.sha1 == null || match.sha1.equals(resource.sha1()))
					) {
						
						response.sendCachedResource(resource);
						
					// if the URI was versioned with a SHA that doesn't match our
					// resource, redirect to the right URI
					} else if (match.versioned) {
						
						response.sendTemporaryRedirect(resource);
						
					// if the URI was not versioned, respond with an uncached resource
					// (but with proper e-tags, if we loaded the resource
					} else {
						
						response.sendUncachedResource(resource);
						
					}
				} catch (Exception e) {
					response.error(e);
				}
			}
			
		};
	}

	/**
	 * @param match
	 * @return
	 */
	public abstract T loadResource(URIMatch match);

}
