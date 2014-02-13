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

import org.mozilla.javascript.Callable;

/**
 * @author jason
 *
 */
public interface ContinuationCoordinator {

	/**
	 * function execution within the context of script environment
	 * @param scriptEnvironment
	 * @param functionName
	 * @param args
	 * @return true if completed, false if continued
	 */
	ContinuationPendingKey execute(ScriptEnvironment scriptEnvironment, Callable function, Object... args);

	/**
	 * Resumes a continuation that was previously saved from an execution in this class
	 * @param pendingKey
	 * @param scriptEnvironment
	 * @param result
	 * @return true if completed, false if continued
	 */
	ContinuationPendingKey resumeContinuation(ScriptEnvironment scriptEnvironment, ContinuationPendingKey pendingKey, Object result);
	
	/**
	 * Resume a continuation, for use by code that constructs its own ContinuationPendingKey, such as from a network message
	 * @param pendingKey
	 * @param result
	 */
	void resume(ContinuationPendingKey pendingKey, Object result);
	
	/**
	 * Pass a task instance in to await a continuation
	 * @param task
	 */
	void awaitContinuation(ScriptTask<? extends ScriptEnvironment> task);

}