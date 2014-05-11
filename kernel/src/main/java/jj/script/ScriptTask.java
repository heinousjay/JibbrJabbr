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

import jj.execution.DelayedTask;
import jj.execution.TaskRunner;

/**
 * The unit of execution for dealing with the script system.
 * 
 * 
 * @author jason
 *
 */
public abstract class ScriptTask<T extends ScriptEnvironment> extends DelayedTask<ScriptExecutor> {
	
	protected final T scriptEnvironment;
	
	protected final ContinuationCoordinator continuationCoordinator;
	
	/** assign the result of any operation against the ContinuationCoordinator to this field */
	protected ContinuationPendingKey pendingKey;
	
	/** 
	 * if this field is populated when the task is run, call the resume on the ContinuationCoordinator
	 * with the stored ContinuationPendingKey and this result, assigning any result of that operation
	 * to the pendingKey field,
	 * 
	 * <pre>
	 * pendingKey = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, result);
	 * </pre>
	 */
	protected Object result;
	
	protected ScriptTask(final String name, final T scriptEnvironment, final ContinuationCoordinator continuationCoordinator) {
		super(name);
		this.scriptEnvironment = scriptEnvironment;
		this.continuationCoordinator = continuationCoordinator;
	}
	
	@Override
	protected final void run() throws Exception {
		
		if (pendingKey == null) {
			begin();
		} else if (result != null) {
			resume();
		} else {
			throw new AssertionError("did you mess with the pendingKey and/or result?\npendingKey = " + pendingKey + "\nresult = " + result);
		}
		
		if (pendingKey == null) {
			complete();
		} else {
			continuationCoordinator.awaitContinuation(this);
		}
	}
	
	/**
	 * Implement this method to perform initial execution of this task.
	 * @throws Exception
	 */
	protected abstract void begin() throws Exception;
	
	/**
	 * Implement this method to have control over resumption.  If all you do is pass the result
	 * to the ContinuationCoordinator, don't worry about it, you're covered
	 * @throws Exception
	 */
	protected void resume() throws Exception {
		pendingKey = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, result);
	}
	
	/**
	 * Implement this method to run after completion of either the begin or resume methods, when no
	 * continuation is pending. this will only be called if the begin/resume methods complete 
	 * successfully, otherwise the errored method will be called
	 * @throws Exception
	 */
	protected void complete() throws Exception {
		
	}
	
	@Override
	protected ScriptExecutor findExecutor(ExecutorFinder executors) {
		return executors.ofType(ScriptExecutorFactory.class).executorFor(scriptEnvironment);
	}

	/**
	 * return null if the task is completed
	 * return the continuation pendingKey if the task needs to be resumed
	 * be sure to store the key for resumption
	 * @return
	 */
	final ContinuationPendingKey pendingKey() {
		return pendingKey;
	}
	
	/**
	 * The {@link TaskRunner} will call this with the result to be used to continue.
	 * do not do any processing in this method! store the value and wait to be run
	 * @param result
	 */
	final void resumeWith(Object result) {
		this.result = result;
	}
}
