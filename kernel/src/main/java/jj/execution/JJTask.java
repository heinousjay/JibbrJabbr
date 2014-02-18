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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

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
public abstract class JJTask implements Delayed {
	
	protected interface ExecutorFinder {
		
		<T> T ofType(Class<T> executorType);
	}
	
	private final String name;
	
	private volatile long maxTime = 0;
	
	private volatile long enqueuedTime = 0;
	
	private volatile long startTime = 0;
	
	private volatile long executionTime = 0;
	
	private final Promise promise = new Promise();
	
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
	
	final void enqueue(long timeoutMillis) {
		executionTime = 0;
		startTime = 0;
		maxTime = timeoutMillis;
		enqueuedTime = System.currentTimeMillis();
	}
	
	final void start() {
		enqueuedTime = 0;
		startTime = System.currentTimeMillis();
	}
	
	final void end() {
		executionTime = System.currentTimeMillis() - startTime;
	}
	
	final boolean enqueued() {
		return enqueuedTime > 0;
	}

	final boolean started() {
		return startTime > 0;
	}
	
	final boolean finished() {
		return executionTime > 0;
	}
	
	final long timeInQueue() {
		return System.currentTimeMillis() - enqueuedTime;
	}
	
	final long executionTime() {
		return executionTime;
	}
	
	final void next(JJTask next) {
		promise.then(next);
	}
	
	final Promise promise() {
		return promise;
	}
	
	@Override
	public final long getDelay(TimeUnit unit) {
		return unit.convert(maxTime - (System.currentTimeMillis() - enqueuedTime), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public final int compareTo(Delayed o) {
		long x = getDelay(TimeUnit.MILLISECONDS);
		long y = o.getDelay(TimeUnit.MILLISECONDS);
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}
	
	@Override
	public final String toString() {
		return new StringBuilder(getClass().getSimpleName()).append(" - ").append(name).toString();
	}
	
}
