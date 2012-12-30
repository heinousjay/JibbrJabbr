package jj.servable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import jj.IOThread;
import jj.request.RequestProcessor;
import jj.webbit.JJHttpRequest;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

public abstract class Servable implements Comparable<Servable> {
	
	/**
	 * Only one should be first and one should be last, which
	 * is the HtmlServable and the AssetServable, respectively.
	 * this isn't the most robust ordering mechanism but it
	 * does the job as well as anything else
	 * @author jason
	 *
	 */
	protected enum Rank {
		First,
		NearFirst,
		Middle,
		NearLast,
		Last;
	}
	
	protected final Path basePath;
	
	protected Servable(final Path basePath) {
		this.basePath = basePath;
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
	
	protected abstract Rank rank();
	
	@Override
	public int compareTo(Servable o) {
		return this.rank().compareTo(o.rank());
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
		final HttpResponse response,
		final HttpControl control
	) throws IOException;
}
