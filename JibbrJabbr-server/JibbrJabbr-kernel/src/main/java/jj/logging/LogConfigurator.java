package jj.logging;

import static jj.logging.LoggingModule.*;
import static ch.qos.logback.classic.Level.*;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.Iterator;

import javax.inject.Singleton;

import jj.JJServerListener;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * ensures that all logging is done through an async appender.
 * TODO - allow external configuration
 * TODO - allow runtime adjustment
 * TODO - make this restartable
 * @author jason
 *
 */
@Singleton
class LogConfigurator implements JJServerListener {
	
	private static final String NETTY_LOGGER = "io.netty";
	
	private static final String JJ_LOGGER = "jj";

	private final Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	private final LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
	
	private final AsyncAppender asyncAppender;
	
	LogConfigurator(boolean isTest) {
		emergencyLogger(TRACE);
		
		if (isTest) {
			this.asyncAppender = null;
			logger.setLevel(OFF);
			testLogger(ERROR);
			traceLogger(ERROR);
			InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		} else {
			this.asyncAppender = initialize();
		}
	}
	
	private AsyncAppender initialize() {
		AsyncAppender asyncAppender = new AsyncAppender();
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
		
		// logs events specifically related to running inside a JJAppTest
		testLogger(OFF);
		
		// make sure netty logs to our log
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		
		// just play with this!
		executionLogging();
		
		return asyncAppender;
	}
	
	protected void executionLogging() {
		logger.setLevel(TRACE);
		
		nettyLogger(OFF);
		
		jjLogger(OFF);
		
		accessLogger(TRACE);
		
		// execution trace logging.  lots of info about the path of execution for interactions
		// with the system
		traceLogger(TRACE);
	}
	
	protected void emergencyLogger(Level level) {
		((Logger)LoggerFactory.getLogger(EMERGENCY_LOGGER)).setLevel(level);
	}
	
	protected void testLogger(Level level) {
		((Logger)LoggerFactory.getLogger(TEST_RUNNER_LOGGER)).setLevel(level);
	}
	
	protected void accessLogger(Level level) {
		((Logger)LoggerFactory.getLogger(ACCESS_LOGGER)).setLevel(level);
	}
	
	protected void traceLogger(Level level) {
		((Logger)LoggerFactory.getLogger(EXECUTION_TRACE_LOGGER)).setLevel(level);
	}
	
	protected void nettyLogger(Level level) {
		((Logger)LoggerFactory.getLogger(NETTY_LOGGER)).setLevel(level);
	}
	
	protected void jjLogger(Level level) {
		((Logger)LoggerFactory.getLogger(JJ_LOGGER)).setLevel(level);
	}
	
	protected void infoAll() {
		logger.setLevel(Level.INFO); // start from clean
	}
	
	protected void traceAll() {
		logger.setLevel(Level.TRACE); // start from clean
	}
	
	@Override
	public void start() throws Exception {
		
	}

	@Override
	public void stop() {
		if (asyncAppender != null) asyncAppender.stop();
	}
}
