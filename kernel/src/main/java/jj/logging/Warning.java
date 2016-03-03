package jj.logging;

import jj.ServerLogger;
import jj.logging.LoggedEvent;
import org.slf4j.Logger;

/**
 * @author jason
 */
@ServerLogger
public class Warning  extends LoggedEvent {

	private final String message;
	private final Object[] args;

	public Warning(String message, Object...args) {
		this.message = message;
		this.args = args;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.warn(message, args);
	}
}
