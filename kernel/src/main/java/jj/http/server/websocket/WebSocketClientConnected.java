package jj.http.server.websocket;

import org.slf4j.Logger;

import jj.ServerLogger;
import jj.logging.LoggedEvent;

/**
 * 
 * @author jason
 *
 */
@ServerLogger
public class WebSocketClientConnected extends LoggedEvent {
	
	private final WebSocketConnection connection;
	
	WebSocketClientConnected(WebSocketConnection connection) {
		this.connection = connection;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.info("New {}", connection);
	}

}
