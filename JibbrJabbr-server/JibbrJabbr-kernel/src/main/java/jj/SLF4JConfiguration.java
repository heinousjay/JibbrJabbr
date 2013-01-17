package jj;

import java.util.Iterator;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * ensures that all logging is done through an async appender.
 * TODO - make this smarter, of course but for now it works
 * @author jason
 *
 */
class SLF4JConfiguration implements JJShutdown {

	private final Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	private final LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
	
	private final AsyncAppender asyncAppender;
	
	SLF4JConfiguration() {
		
		asyncAppender = new AsyncAppender();
		asyncAppender.setContext(context);
		
		Iterator<Appender<ILoggingEvent>> i = logger.iteratorForAppenders();
		while (i.hasNext()) {
			Appender<ILoggingEvent> appender = i.next();
			logger.detachAppender(appender);
			appender.setContext(context);
			asyncAppender.addAppender(appender);
		}
		
		asyncAppender.start();
		
		logger.addAppender(asyncAppender);
		
		// make sure netty logs to our log
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		
		// shutting up the logging in our dependencies by default
		((Logger)LoggerFactory.getLogger("com")).setLevel(Level.ERROR);
		((Logger)LoggerFactory.getLogger("org")).setLevel(Level.ERROR);
	}

	@Override
	public void stop() {
		asyncAppender.stop();
	}
}
