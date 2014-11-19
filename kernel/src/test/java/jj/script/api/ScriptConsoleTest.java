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
package jj.script.api;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;

import jj.event.MockPublisher;
import jj.script.CurrentScriptEnvironment;
import jj.script.module.RootScriptEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptConsoleTest {
	
	@Mock RootScriptEnvironment se;
	@Mock CurrentScriptEnvironment env;
	MockPublisher publisher;
	
	ScriptConsole sc;
	
	@Mock Logger logger;
	
	@Before
	public void before() {
		given(se.name()).willReturn("some/path");
		given(env.current()).willReturn(se);
		given(env.currentRootScriptEnvironment()).willReturn(se);
		publisher = new MockPublisher();
		
		sc = new ScriptConsole(env, publisher);
		
		given(logger.isTraceEnabled()).willReturn(true);
		given(logger.isDebugEnabled()).willReturn(true);
		given(logger.isInfoEnabled()).willReturn(true);
		given(logger.isWarnEnabled()).willReturn(true);
		given(logger.isErrorEnabled()).willReturn(true);
	}

	@Test
	public void testTrace() {
		// when
		sc.trace(Arrays.asList("arg1", "arg2"));
		
		// when
		ConsoleLoggingEvent cle = (ConsoleLoggingEvent)publisher.events.get(0);
		cle.describeTo(logger);
		
		// then
		assertThat(cle.loggerName(), is("script@some/path"));
		verify(logger).trace("arg1 arg2");
	}

	@Test
	public void testDebug() {
		// when
		sc.debug(Arrays.asList("arg1", "arg2"));
		
		// when
		ConsoleLoggingEvent cle = (ConsoleLoggingEvent)publisher.events.get(0);
		cle.describeTo(logger);
		
		// then
		assertThat(cle.loggerName(), is("script@some/path"));
		verify(logger).debug("arg1 arg2");
	}

	@Test
	public void testInfo() {
		// when
		sc.info(Arrays.asList("arg1", "arg2"));
		
		// when
		ConsoleLoggingEvent cle = (ConsoleLoggingEvent)publisher.events.get(0);
		cle.describeTo(logger);
		
		// then
		assertThat(cle.loggerName(), is("script@some/path"));
		verify(logger).info("arg1 arg2");
	}

	@Test
	public void testWarn() {
		// when
		sc.warn(Arrays.asList("arg1", "arg2"));
		
		// when
		ConsoleLoggingEvent cle = (ConsoleLoggingEvent)publisher.events.get(0);
		cle.describeTo(logger);
		
		// then
		assertThat(cle.loggerName(), is("script@some/path"));
		verify(logger).warn("arg1 arg2");
	}

	@Test
	public void testError() {
		// when
		sc.error(Arrays.asList("arg1", "arg2"));
		
		// when
		ConsoleLoggingEvent cle = (ConsoleLoggingEvent)publisher.events.get(0);
		cle.describeTo(logger);
		
		// then
		assertThat(cle.loggerName(), is("script@some/path"));
		verify(logger).error("arg1 arg2");
	}

}
