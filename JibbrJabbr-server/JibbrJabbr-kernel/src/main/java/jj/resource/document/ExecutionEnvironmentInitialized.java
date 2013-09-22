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

import jj.execution.ExecutionEvent;
import jj.script.ScriptExecutionEnvironment;

/**
 * @author jason
 *
 */
public class ExecutionEnvironmentInitialized implements ExecutionEvent {
	
	private final ScriptExecutionEnvironment executionEnvironment;

	public ExecutionEnvironmentInitialized(final ScriptExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}
	
	public ScriptExecutionEnvironment executionEnvironment() {
		return executionEnvironment;
	}
}