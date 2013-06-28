package jj.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
import jj.http.RequestProcessor;

@Singleton
class CssServable extends Servable {
	
	private static final String CSS = ".css";
	
	@Inject
	CssServable(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public boolean isMatchingRequest(final JJHttpRequest request) {
		// match everything that ends in .css
		return request.uri().endsWith(CSS);
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		JJHttpRequest request,
		JJHttpResponse response
	) throws IOException {
		
		// if this is a plain URL (no sha key) then find the latest matching css
		// and issue a redirect to that URL
		
		
		return null;
	}
}
