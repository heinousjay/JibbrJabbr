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
package jj.document;

import org.slf4j.Logger;

import jj.execution.ExecutionEvent;

/**
 * @author jason
 *
 */
class CompilingServerScript extends ExecutionEvent {

	private final String path;
	
	CompilingServerScript(final String path) {
		this.path = path;
	}

	@Override
	public void describeTo(Logger log) {
		log.info("compiling server script at {}", path);
	}
}
