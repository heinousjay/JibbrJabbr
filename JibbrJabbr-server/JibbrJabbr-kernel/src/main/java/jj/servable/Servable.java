package jj.servable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import jj.IOThread;
import jj.configuration.Configuration;
import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
import jj.http.RequestProcessor;

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
	 * flag to indicate if loading processing this resource should happen
	 * in an IO thread
	 * @return
	 */
	public boolean needsIO(final JJHttpRequest request) {
		return false;
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
	public abstract boolean isMatchingRequest(final JJHttpRequest httpRequest);
	
	public abstract RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final JJHttpResponse response
	) throws IOException;

	
	
	/**
	 * helper to extract the baseName from the request
	 * @param httpRequest
	 * @return
	 */
	protected String baseName(JJHttpRequest httpRequest) {
		return httpRequest.uri().substring(1);
	}
}
