package jj.logging;

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
	    encoder.setPattern("%-5level %date{HH:mm:ss.SSS} %logger: %message >> [%mdc{thread}]%n%rootException");
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
 		
 		accessLogger(TRACE);
 		
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
 		logger(NETTY_LOGGER, OFF);
	}
	
	protected void executionLogging() {
		rootLogger.setLevel(TRACE);
		
		jjLogger(OFF);
		
		accessLogger(TRACE);
		
		// execution trace logging.  lots of info about the path of execution for interactions
		// with the system
		traceLogger(TRACE);
	}
	
	private void logger(String name, Level level) {
		((Logger)LoggerFactory.getLogger(name)).setLevel(level);
	}
	
	protected void emergencyLogger(Level level) {
		logger("emergency", level);
	}
	
	protected void testLogger(Level level) {
		logger("test runner", level);
	}
	
	protected void accessLogger(Level level) {
		logger("access", level);
	}
	
	protected void traceLogger(Level level) {
		logger("execution trace", level);
	}
	
	protected void serverLogger(Level level) {
		logger("server", level);
	}
	
	protected void jjLogger(Level level) {
		logger(JJ_LOGGER, level);
	}
	
	protected void infoAll() {
		rootLogger.setLevel(INFO);
	}
	
	protected void traceAll() {
		rootLogger.setLevel(TRACE);
	}
}
