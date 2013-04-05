package jj.servable;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Configuration;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

@Singleton
class CssServable extends Servable {
	
	private static final Pattern CSS_RESOURCE = Pattern.compile("^/(.+?)/([a-f0-9]{40}).css$");
	
	@Inject
	CssServable(final Configuration configuration) {
		super(configuration);
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
