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

import jj.resource.ResourceTask;
import jj.script.ScriptTask;

/**
 * <p>
 * provides the base facilities for task management, and
 * an API for task construction.  Only derive from this directly
 * if making a new type of task, otherwise {@link ResourceTask} or
 * {@link ScriptTask} are what you're looking for.
 * </p>
 * 
 * <p>
 * After constructing a task instance, submit it to the {@link TaskRunner}
 * for scheduling
 * </p>
 * 
 * @author jason
 *
 */
public abstract class JJTask {
	
	protected interface ExecutorFinder {
		
		<T> T ofType(Class<T> executorType);
	}
	
	private final String name;
	
	private final String spawnedBy = Thread.currentThread().getName();
	
	private final Promise promise = new Promise();
	
	// ugly! but certain threads need to be interruptable explicitly
	volatile Thread runningThread;
	
	public void interrupt() {
		try {
			runningThread.interrupt();
		} catch (NullPointerException npe) {
			// eat on purpose, it's a no-op if any of the chain is null anyway
		}
	}
	
	protected JJTask(final String name) {
		this.name = name;
	}

	protected abstract void run() throws Exception;
	
	/**
	 * called when an error occurs during task execution.  return true if you consider
	 * the error handled
	 */
	protected boolean errored(Throwable cause) {
		return false;
	}
	
	protected abstract void addRunnableToExecutor(ExecutorFinder executors, Runnable runnable);
	
	final String name() {
		return name;
	}
	
	final void next(JJTask next) {
		promise.then(next);
	}
	
	final Promise promise() {
		return promise;
	}
	
	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(" - ").append(name);
		sb.append(" spawned by: ").append(spawnedBy);
		return sb.toString();
	}
	
}
