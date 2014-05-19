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

import jj.JJServerStartupListener;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import jj.util.Closer;

/**
 * Coordinates asynchronous logging with the logging configuration system.  Also provides
 * the implementation of the EmergencyLog for now, but that may change and/or go away
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class SystemLogger implements JJServerStartupListener {
	
	static final String THREAD_NAME = "thread";
	
	private final BlockingQueue<LoggedEvent> events = new LinkedBlockingQueue<>();
	
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
					LoggedEvent event = events.take();
					Logger logger = loggers.findLogger(event);
					try (Closer closer = threadName(event.threadName)) {
						event.describeTo(logger);
					}
				}
			}
		});
	}
	
	// package private because it is exposed in a test class
	Closer threadName(String threadName) {
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
		events.add(event);
	}
	

	
	@Override
	public Priority startPriority() {
		return Priority.Highest;
	}

}
