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
package jj.testing;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import jj.event.Publisher;
import jj.logging.LoggedEvent;

/**
 * obviously this class is ridiculous.  get rid of it
 * @author jason
 *
 */
@Singleton
public class TestLog {
	
	private enum Level { ERROR, WARN, INFO, DEBUG, TRACE }
	
	private final Publisher publisher;
	
	@Inject
	TestLog(final Publisher publisher) {
		this.publisher = publisher;
	}
	
	@TestRunnerLogger
	private static class TestEvent implements LoggedEvent {

		private final Level trace;
		private final String message;
		private final Object[] args;
		
		public TestEvent(Level trace, String message, Object[] args) {
			this.trace = trace;
			this.message = message;
			this.args = args;
		}

		@Override
		public void describeTo(Logger logger) {
			switch (trace) {
			
			case TRACE:
				logger.trace(message, args);
				break;
				
			case DEBUG:
				logger.debug(message, args);
				break;
				
			case INFO:
				logger.info(message, args);
				break;
				
			case WARN:
				logger.warn(message, args);
				
			case ERROR:
				logger.error(message, args);
			}
		}
	}
	
	public void debug(String message, Object...args) {
		publisher.publish(new TestEvent(Level.DEBUG, message, args));
	}
	
	public void info(String message, Object...args) {
		publisher.publish(new TestEvent(Level.INFO, message, args));
	}
	
	public void trace(String message, Object...args) {
		publisher.publish(new TestEvent(Level.TRACE, message, args));
	}

}
