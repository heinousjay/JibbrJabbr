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
package jj.resource.document;

import org.slf4j.Logger;

import jj.execution.ExecutionEvent;

/**
 * @author jason
 *
 */
class ErrorEvaluatingClientStub implements ExecutionEvent {

	private final String path;
	private final Throwable cause;
	
	ErrorEvaluatingClientStub(final String path, final Throwable cause) {
		this.path = path;
		this.cause = cause;
	}

	@Override
	public void describeTo(Logger log) {
		log.error("error evaluating client script at {}", path);
		log.error("", cause);
	}
}
