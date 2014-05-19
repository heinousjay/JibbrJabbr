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

import org.slf4j.Logger;

import jj.logging.LoggedEvent;

/**
 * @author jason
 *
 */
public class CannotFindContinuation extends LoggedEvent {

	private final ScriptEnvironment scriptEnvironment;
	private final ContinuationPendingKey pendingKey;
	
	CannotFindContinuation(final ScriptEnvironment scriptEnvironment, final ContinuationPendingKey pendingKey) {
		this.scriptEnvironment = scriptEnvironment;
		this.pendingKey = pendingKey;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.error("attempting to resume a non-existent continuation in {} keyed by {}", scriptEnvironment, pendingKey);
		logger.error("helpful stacktrace", new Exception());
	}

}
