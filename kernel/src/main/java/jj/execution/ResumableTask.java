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

import jj.script.ContinuationPendingKey;

/**
 * implement this to notify the executor system that your task can be resumed
 * 
 * @author jason
 *
 */
public abstract class ResumableTask extends JJTask {
	
	protected ContinuationPendingKey pendingKey;
	protected Object result;
	
	/**
	 * @param name
	 */
	protected ResumableTask(String name) {
		super(name);
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
