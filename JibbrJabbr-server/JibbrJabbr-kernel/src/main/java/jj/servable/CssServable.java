package jj.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

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
		HttpResponse response,
		HttpControl control
	) throws IOException {
		
		// if this is a plain URL (no sha key) then find the latest matching css
		// and issue a redirect to that URL
		
		
		return null;
	}
}
