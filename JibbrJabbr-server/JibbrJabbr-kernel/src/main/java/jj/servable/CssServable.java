package jj.servable;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

@Singleton
class CssServable extends Servable {
	
	private static final Pattern CSS_RESOURCE = Pattern.compile(".css$");
	
	@Inject
	CssServable(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public boolean isMatchingRequest(final JJHttpRequest request) {
		// match everything that ends in .css
		return CSS_RESOURCE.matcher(request.uri()).matches();
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		JJHttpRequest request,
		HttpResponse response,
		HttpControl control
	) throws IOException {
		
		// if this is a plain URL (no sha key) then find the latest matching css
		// and issue a redirect to that URL
		
		
		return null;
	}
}
