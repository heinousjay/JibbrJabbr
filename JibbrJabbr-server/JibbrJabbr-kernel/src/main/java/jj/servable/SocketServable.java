package jj.servable;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Configuration;
import jj.ExecutionTrace;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebSocketHandler;

/**
 * upgrades to a websocket connection for an
 * incoming socket URI that matches something
 * previous initialized script bundle
 * @author jason
 *
 */
@Singleton
class SocketServable extends Servable {
	
	private final WebSocketHandler webSocketHandler;
	
	private final ExecutionTrace trace;
	
	@Inject
	SocketServable(
		final Configuration configuration,
		final WebSocketHandler webSocketHandler,
		final ExecutionTrace trace
	) {
		super(configuration);
		this.webSocketHandler = webSocketHandler;
		this.trace = trace;
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
		final HttpResponse response, 
		final HttpControl control
	) throws IOException {
		
		trace.addEvent("socket connection incoming");
		
		// 
		return new RequestProcessor() {
			
			@Override
			public void process() {
				// this is the appropriate place to integrate socket.io's transport mechanisms
				// if it becomes worthwhile to support downbrowser crap
				control.upgradeToWebSocketConnection(webSocketHandler);
				
				// the request is considered over at this point, we are connection
				trace.addEvent("socket connection made");
				trace.end(request.originalRequest());
			}
		};
	}

}
