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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.MDC;

import jj.Closer;
import jj.JJServerStartupListener;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;

/**
 * Coordinates asynchronous logging with the logging configuration system.  Also provides
 * the implementation of the EmergencyLog for now, but that may change and/or go away
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class SystemLogger implements EmergencyLog, JJServerStartupListener {
	
	static final String THREAD_NAME = "thread";

	private static class EventBundle {
		final LoggedEvent event;
		final String threadName;
		
		EventBundle(LoggedEvent event) {
			this.event = event;
			this.threadName = Thread.currentThread().getName();
		}
	}
	
	private final BlockingQueue<EventBundle> events = new LinkedBlockingQueue<>();
	
	private final TaskRunner taskRunner;
	
	private final Loggers loggers;
	
	@Inject
	SystemLogger(final TaskRunner taskRunner, final Loggers loggers) {
		this.taskRunner = taskRunner;
		this.loggers = loggers;
	}
	


	@Override
	public void start() throws Exception {
		taskRunner.execute(new ServerTask("System Logger") {
			
			@Override
			protected void run() throws Exception {
				for (;;) {
					EventBundle bundle = events.take();
					Logger logger = loggers.findLogger(bundle.event);
					try (Closer closer = threadName(bundle.threadName)) {
						System.out.println("hi asshole " + bundle.threadName);
						bundle.event.describeTo(logger);
					}
					
				}
			}
		});
	}
	
	private Closer threadName(String threadName) {
		MDC.put(THREAD_NAME, threadName);
		return new Closer() {
			
			@Override
			public void close() {
				MDC.remove(THREAD_NAME);
			}
		};
	}
	
	/**
	 * The actual main interface to the logging system, not intended for
	 * direct use.  Publish some correctly annotated descendent of
	 * LoggedEvent instead.
	 */
	@Listener
	void log(LoggedEvent event) {
		events.add(new EventBundle(event));
	}
	

	
	@EmergencyLogger
	private static class Emergency implements LoggedEvent {
		
		private final boolean error;
		private final String message;
		private final Throwable t;
		private final Object[] args;
		
		Emergency(boolean error, String message, Throwable t) {
			this.error = error;
			this.message = message;
			this.t = t;
			this.args = null;
		}
		
		Emergency(boolean error, String message, Object...args) {
			this.error = error;
			this.message = message;
			this.t = null;
			this.args = args;
		}

		@Override
		public void describeTo(Logger logger) {
			if (error && t != null) {
				logger.error(message, t);
			} else if (error) {
				logger.error(message, args);
			} else {
				logger.warn(message, args);
			}
		}
		
	}
	
	@Override
	public void error(String message, Object... args) {
		log(new Emergency(true, message, args));
	}
	
	@Override
	public void error(String message, Throwable t) {
		log(new Emergency(true, message, t));
	}
	
	@Override
	public void warn(String message, Object... args) {
		log(new Emergency(false, message, args));
	}

	@Override
	public Priority startPriority() {
		return Priority.Highest;
	}

}
