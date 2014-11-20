/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.logging;

import java.util.Map;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.ScriptableObject;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.script.Global;
import jj.script.RhinoContext;

/**
 * @author jason
 *
 */
@Singleton
@Subscriber
public class LoggingConfigurator {

	private static final Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	private static final LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
	
	static {
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
		
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
	}
	
	private final LoggingConfiguration config;
	
	private final ScriptableObject loggerNames;
	
	private final LogLevelDefaultProvider logLevelDefaultProvider;

	@Inject
	LoggingConfigurator(
		final LoggingConfiguration config,
		final @LoggerNames Map<String, String> loggerNames,
		final @Global ScriptableObject global,
		final Provider<RhinoContext> contextProvider,
		final LogLevelDefaultProvider logLevelDefaultProvider
	) {
		this.config = config;
		
		try (RhinoContext context = contextProvider.get()) {
			this.loggerNames = context.newObject(global);
			for (String name : loggerNames.keySet()) {
				this.loggerNames.put(name, this.loggerNames, loggerNames.get(name));
			}
		}
		
		this.logLevelDefaultProvider = logLevelDefaultProvider;
		setLevels();
	}
	
	@Listener
	void configurationLoaded(ConfigurationLoaded event) {
		setLevels();
	}
	
	public ScriptableObject loggerNames() {
		return loggerNames;
	}
	
	private void setLevels() {
		logger(Logger.ROOT_LOGGER_NAME, Level.OFF);
		setLevels(logLevelDefaultProvider.get());
		setLevels(config.loggingLevels());
		logger(EmergencyLogger.NAME, Level.TRACE);
	}
	
	private void setLevels(Map<String, jj.logging.Level> levelMap) {
		for (String logger : levelMap.keySet()) {
			logger(logger, levelMap.get(logger).logbackLevel());
		}
	}
	
	private void logger(String name, Level level) {
		((Logger)LoggerFactory.getLogger(name)).setLevel(level);
	}
}
