package jj.servable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

class CssServable extends Servable {
	
	private static final Pattern CSS_RESOURCE = Pattern.compile("^/(.+?)/([a-f0-9]{40}).css$");

	CssServable(final Path basePath) {
		super(basePath);
	}
	
	@Override
	protected Rank rank() {
		return Rank.Middle;
	}

	@Override
	public boolean isMatchingRequest(final JJHttpRequest request) {
		// we only serve stuff that looks like 
		//  baseName/sha1.css.  I say looks like
		// because it's not a real baseName, but it
		// is the path to the file.  this is CSS that
		// got pre-processed by a filter to be long-term
		// cacheable, and potentially was processed by
		// less
		return CSS_RESOURCE.matcher(request.uri()).matches();
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		JJHttpRequest request,
		HttpResponse response,
		HttpControl control
	) throws IOException {
		
		return null;
	}
}
