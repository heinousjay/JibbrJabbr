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
	private final Throwable t;
	private final Object[] args;

	public Warning(String message, Throwable t) {
		this.message = message;
		this.t = t;
		this.args = null;
	}

	public Warning(String message, Object...args) {
		this.message = message;
		this.t = null;
		this.args = args;
	}

	@Override
	public void describeTo(Logger logger) {
		if (t != null) {
			logger.warn(message, t);
		} else {
			logger.warn(message, args);
		}
	}
}
