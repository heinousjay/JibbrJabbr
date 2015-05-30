package jj.http.server;

import org.slf4j.Logger;

import jj.logging.LoggedEvent;

public class HttpServerRestarting extends LoggedEvent {

	HttpServerRestarting() {}

	@Override
	public void describeTo(Logger logger) {
		logger.info("HTTP server restarting");
	}

}
