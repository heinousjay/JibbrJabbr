package jj.servable;

import java.io.IOException;
import java.nio.file.Path;
import jj.script.ScriptBundleFinder;
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
class SocketServable extends Servable {

	private final ScriptBundleFinder scriptBundleFinder;
	
	private final WebSocketHandler webSocketHandler;
	
	SocketServable(
		final ScriptBundleFinder scriptBundleFinder,
		final WebSocketHandler webSocketHandler
	) {
		super(null);
		this.scriptBundleFinder = scriptBundleFinder;
		this.webSocketHandler = webSocketHandler;
	}
	
	@Override
	protected Rank rank() {
		return Rank.Middle;
	}
	
	@Override
	protected boolean isServablePath(Path result) {
		// we never serve
		return false;
	}

	@Override
	public boolean isMatchingRequest(final JJHttpRequest request) {
		return scriptBundleFinder.forSocketUriBaseName(request.uri()) != null;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request, 
		final HttpResponse response, 
		final HttpControl control
	) throws IOException {
		
		// 
		return new RequestProcessor() {
			
			@Override
			public void process() {
				// this is the appropriate place to integrate socket.io's transport mechanisms
				// if it becomes worthwhile to support downbrowser crap
				control.upgradeToWebSocketConnection(webSocketHandler);
			}
		};
	}

}
