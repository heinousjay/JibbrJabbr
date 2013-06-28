package jj.servable;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
import jj.http.JJWebSocketHandler;
import jj.http.RequestProcessor;

/**
 * upgrades to a websocket connection for an
 * incoming socket URI that matches something
 * previous initialized script bundle
 * @author jason
 *
 */
@Singleton
class SocketServable extends Servable {
	
	private final JJWebSocketHandler webSocketHandler;
	
	@Inject
	SocketServable(
		final Configuration configuration,
		final JJWebSocketHandler webSocketHandler
	) {
		super(configuration);
		this.webSocketHandler = webSocketHandler;
	}
	
	@Override
	protected boolean isServablePath(Path result) {
		// we never serve
		return false;
	}

	@Override
	public boolean isMatchingRequest(final JJHttpRequest request) {
		// we match on anything that ends with .socket.  when it gets connected,
		// if we can't find the bundle, we force the reload
		return request.uri().endsWith(".socket");
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request, 
		final JJHttpResponse response
	) throws IOException {
		
		// 
		return new RequestProcessor() {
			
			@Override
			public void process() {
				// this is the appropriate place to integrate socket.io's transport mechanisms
				// if it becomes worthwhile to support downbrowser crap
				
				//TODO make this work
				//control.upgradeToWebSocketConnection(webSocketHandler);
			}
		};
	}

}
