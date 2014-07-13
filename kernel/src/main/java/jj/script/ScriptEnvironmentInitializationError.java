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
package jj.script;

import jj.logging.EmergencyLogger;
import jj.logging.LoggedEvent;

import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@EmergencyLogger
public class ScriptEnvironmentInitializationError extends LoggedEvent {
	
	private final ScriptEnvironment scriptEnvironment;
	private final Throwable cause;

	public ScriptEnvironmentInitializationError(final ScriptEnvironment scriptEnvironment, final Throwable cause) {
		this.scriptEnvironment = scriptEnvironment;
		this.cause = cause;
	}
	
	public ScriptEnvironment scriptEnvironment() {
		return scriptEnvironment;
	}
	
	public Throwable cause() {
		return cause;
	}

	@Override
	public void describeTo(Logger log) {
		log.error("script environment errored during initialization: {}", scriptEnvironment);
		log.error("", cause);
	}
}
