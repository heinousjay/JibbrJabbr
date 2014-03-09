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

import org.slf4j.Logger;

import jj.event.Publisher;
import jj.logging.LoggedEvent;

/**
 * @author jason
 *
 */
public class TestLog {
	
	private final Publisher publisher;
	
	@Inject
	TestLog(final Publisher publisher) {
		this.publisher = publisher;
	}
	
	@TestRunnerLogger
	private static class TestEvent implements LoggedEvent {

		private final boolean trace;
		private final String message;
		private final Object[] args;
		
		public TestEvent(boolean trace, String message, Object[] args) {
			this.trace = trace;
			this.message = message;
			this.args = args;
		}

		@Override
		public void describeTo(Logger logger) {
			if (trace) {
				logger.trace(message, args);
			} else {
				logger.info(message, args);
			}
		}
	}
	
	public void info(String message, Object...args) {
		publisher.publish(new TestEvent(false, message, args));
	}
	
	public void trace(String message, Object...args) {
		publisher.publish(new TestEvent(true, message, args));
	}

}
