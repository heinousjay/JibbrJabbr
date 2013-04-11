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

import jj.JJModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provides;

/**
 * @author jason
 *
 */
public class LoggingModule extends JJModule {
	
	public static final String ACCESS_LOGGER = "access";
	public static final String TEST_RUNNER_LOGGER = "test runner";
	public static final String EXECUTION_TRACE_LOGGER = "execution trace";
	
	private final boolean isTest;
	
	public LoggingModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		// this gets instantiated before anything might write to a log
		bind(LogConfigurator.class).toInstance(new LogConfigurator(isTest));
		addServerListenerBinding().to(LogConfigurator.class);
		
	}
	
	@Provides @AccessLogger
	public Logger provideAccessLogger() {
		return LoggerFactory.getLogger(ACCESS_LOGGER);
	}
	
	@Provides @TestRunnerLogger
	public Logger provideTestRunnerLogger() {
		return LoggerFactory.getLogger(TEST_RUNNER_LOGGER);
	}
	
	@Provides @ExecutionTraceLogger
	public Logger provideExecutionTraceLogger() {
		return LoggerFactory.getLogger(EXECUTION_TRACE_LOGGER);
	}

}