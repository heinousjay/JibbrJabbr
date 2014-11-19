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
package jj.execution;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.logging.Emergency;

/**
 * @author jason
 *
 */
@Singleton
class JJUncaughtExceptionHandler implements UncaughtExceptionHandler {
	
	private final Publisher publisher;
	
	@Inject
	JJUncaughtExceptionHandler(final Publisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		
		publisher.publish(new Emergency("uncaught exception in thread " + t, e));
	}

}
