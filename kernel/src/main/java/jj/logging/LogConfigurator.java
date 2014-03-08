package jj.logging;

import static jj.logging.LoggingModule.*;
import static ch.qos.logback.classic.Level.*;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import javax.inject.Singleton;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * This class needs a wash!
 *
 */
@Singleton
class LogConfigurator {
	
	private static final String NETTY_LOGGER = "io.netty";
	
	private static final String JJ_LOGGER = "jj";

	private final Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	private final LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
	
	LogConfigurator(boolean isTest) {
		initialize();
		
		emergencyLogger(TRACE);
		serverLogger(INFO);
		
		if (isTest) {
			rootLogger.setLevel(OFF);
			testLogger(INFO);
			traceLogger(TRACE);
		}
	}
	
	private void initialize() {
		
	    // we are not interested in auto-configuration
	    loggerContext.reset();

	    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
	    encoder.setContext(loggerContext);
	    // %highlight(%-5level) %cyan(%logger{15})
	    encoder.setPattern("%date{HH:mm:ss.SSS} [%mdc{thread}] %-5level %logger - %message%n%rootException");
	    encoder.start();

	    ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
	    appender.setContext(loggerContext);
	    appender.setEncoder(encoder); 
	    appender.start();

	    rootLogger.addAppender(appender);
	    
	    // logs events specifically related to running inside a JJAppTest
 		testLogger(OFF);
 		
 		// just play with this!
 		infoAll();
 		
 		accessLogger(OFF);
 		
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
	}
	
	protected void executionLogging() {
		rootLogger.setLevel(TRACE);
		
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
		((Logger)LoggerFactory.getLogger("execution trace")).setLevel(level);
	}
	
	protected void serverLogger(Level level) {
		((Logger)LoggerFactory.getLogger("server")).setLevel(level);
	}
	
	protected void nettyLogger(Level level) {
		((Logger)LoggerFactory.getLogger(NETTY_LOGGER)).setLevel(level);
	}
	
	protected void jjLogger(Level level) {
		((Logger)LoggerFactory.getLogger(JJ_LOGGER)).setLevel(level);
	}
	
	protected void infoAll() {
		rootLogger.setLevel(INFO);
	}
	
	protected void traceAll() {
		rootLogger.setLevel(TRACE);
	}
}
