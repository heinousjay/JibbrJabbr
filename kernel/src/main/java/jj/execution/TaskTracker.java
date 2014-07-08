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

import java.lang.ref.WeakReference;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import jj.ServerLogger;
import jj.logging.LoggedEvent;
import jj.util.Clock;

import org.slf4j.Logger;

@ServerLogger
class TaskTracker extends LoggedEvent implements Delayed {
	
	static final long MAX_QUEUED_TIME = TimeUnit.SECONDS.toMillis(10);
	
	private volatile long maxTime = 0;
	
	private volatile long enqueuedTime = 0;
	
	private volatile long startTime = 0;
	
	private volatile long executionTime = 0;
	
	private volatile boolean endedInError;
	
	private final Clock clock;
	
	private final String name;
	
	private final WeakReference<JJTask> task;
	
	TaskTracker(final Clock clock, final JJTask tracked) {
		this.clock = clock;
		task = new WeakReference<>(tracked);
		name = tracked.name();
	}
	
	@Override
	public final long getDelay(TimeUnit unit) {
		return unit.convert(maxTime - (clock.time() - enqueuedTime), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public final int compareTo(Delayed o) {
		long x = getDelay(TimeUnit.MILLISECONDS);
		long y = o.getDelay(TimeUnit.MILLISECONDS);
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}
	
	void enqueue() {
		assert (maxTime == 0);
		
		long timeoutMillis = MAX_QUEUED_TIME;
		
		// don't get upset 
		if (task.get() instanceof DelayedTask) {
			timeoutMillis = ((DelayedTask<?>)task.get()).delay() + MAX_QUEUED_TIME;
		}
		
		executionTime = 0;
		startTime = 0;
		maxTime = timeoutMillis;
		enqueuedTime = clock.time();
	}
	
	void start() {
		assert (maxTime != 0 && startTime == 0);
		startTime = clock.time();
	}
	
	void end() {
		assert (startTime != 0 && executionTime == 0);
		executionTime = clock.time() - startTime;
	}
	
	void endedInError() {
		endedInError = true;
	}
	
	long enqueuedTime() {
		return startTime - enqueuedTime;
	}
	
	long startTime() {
		return startTime;
	}

	long executionTime() {
		return executionTime;
	}
	
	JJTask task() {
		return task.get();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + 
			" { enqueuedTime: " + enqueuedTime() + 
			", startTime: " + startTime() + 
			", executionTime: " + executionTime() +
			", task:" + task() +
			"}";
	}
	
	@Override
	public void describeTo(Logger logger) {
		if (logger.isTraceEnabled()) {
			// i agree, this is ugly
			JJTask task = this.task.get();
			boolean logged = false;
			if (task instanceof DelayedTask) {
				DelayedTask<?> dTask = (DelayedTask<?>)task;
				if (dTask.delay() > 0L) {
					logger.trace(
						"delayed task {} completed in {} millis, delayed for {} millis, waited for {} millis{}{}",
						name, executionTime, dTask.delay(), enqueuedTime(), (dTask.willRepeat ? ", will repeat" : ""), (endedInError ? ", ended in error" : "")
					);
					logged = true;
				}
			}
			
			if (!logged) {
				logger.trace("task {} completed in {} millis, waited for {} millis{}", name, executionTime, enqueuedTime(), (endedInError ? ", ended in error" : ""));
			}
		}
	}
}