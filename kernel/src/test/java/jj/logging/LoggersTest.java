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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

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
public class LoggersTest {
	
	private static final String NAMES_LOGGER_EVENT = "names logger event";

	@Retention(RetentionPolicy.RUNTIME)
	private @interface Ann1 {}
	
	@Ann1
	private class Ann1Event extends LoggedEvent {
		@Override
		public void describeTo(Logger logger) {}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Ann2 {}

	@Ann2
	private class Ann2Event extends LoggedEvent {
		@Override
		public void describeTo(Logger logger) {}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Ann3 {}

	@Ann3
	private class Ann3Event extends LoggedEvent {
		@Override
		public void describeTo(Logger logger) {}
	}
	
	private class NamesLoggerEvent extends LoggedEvent implements NamesLogger {

		@Override
		public void describeTo(Logger logger) {}
		
		@Override
		public String loggerName() {
			return NAMES_LOGGER_EVENT;
		}
	}
	
	private @Mock Logger logger1;
	private @Mock Logger logger2;
	private @Mock Logger logger3;
	
	@Test
	public void test() {
		
		// given
		Map<Class<? extends Annotation>, Logger> loggers = new HashMap<>();
		loggers.put(Ann1.class, logger1);
		loggers.put(Ann2.class, logger2);
		loggers.put(Ann3.class, logger3);
		Loggers l = new Loggers(loggers);
		
		// when, then
		assertThat(l.findLogger(new Ann1Event()), is(logger1));
		assertThat(l.findLogger(new Ann2Event()), is(logger2));
		assertThat(l.findLogger(new Ann3Event()), is(logger3));
		
		Logger logger = l.findLogger(new NamesLoggerEvent());
		
		assertThat(logger.getName(), is(NAMES_LOGGER_EVENT));
	}

}
