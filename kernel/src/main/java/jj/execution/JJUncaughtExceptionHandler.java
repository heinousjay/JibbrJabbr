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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.logging.EmergencyLogger;

/**
 * @author jason
 *
 */
@Singleton
class JJUncaughtExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// this is a bad failure! almost certainly an assertion
		// so we just log it directly
		Logger logger = LoggerFactory.getLogger(EmergencyLogger.NAME);
		
		logger.error("uncaught exception in thread {}", t);
		logger.error("", e);
		
		// need to respond 500 if this is a web request.
		// this is going to require the trace facility
		// not sure how to proceed if handling an event
		// probably need to send a reload command
	}

}
