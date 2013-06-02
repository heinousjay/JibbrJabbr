package jj.logging;

import static jj.logging.LoggingModule.*;

import java.util.Iterator;

import javax.inject.Singleton;

import jj.JJServerListener;

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
@Singleton
class LogConfigurator implements JJServerListener {

	private final Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	private final LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
	
	private final AsyncAppender asyncAppender;
	
	LogConfigurator(boolean isTest) {
		
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
		
		logger.setLevel(Level.DEBUG); // start from clean
		
		((Logger)LoggerFactory.getLogger("jj")).setLevel(isTest ? Level.INFO : Level.TRACE);
		
		// logs events specifically related to running inside a JJAppTest
		((Logger)LoggerFactory.getLogger(TEST_RUNNER_LOGGER)).setLevel(Level.OFF);
		
		// the http access log
		((Logger)LoggerFactory.getLogger(ACCESS_LOGGER)).setLevel(Level.OFF);
		
		// execution trace logging.  lots of info about the path of execution for interactions
		// with the system
		((Logger)LoggerFactory.getLogger(EXECUTION_TRACE_LOGGER)).setLevel(Level.INFO);
		
	}
	
	@Override
	public void start() throws Exception {
		// nothing to do here
	}

	@Override
	public void stop() {
		if (asyncAppender != null) asyncAppender.stop();
	}
}
