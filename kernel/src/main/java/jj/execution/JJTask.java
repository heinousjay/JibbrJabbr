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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author jason
 *
 */
public abstract class JJTask implements Delayed {
	
	private static final long MAX_QUEUED_TIME = TimeUnit.SECONDS.toMillis(20);
	
	private final String name;
	
	private volatile long enqueuedTime = 0;
	
	private volatile long startTime = 0;
	
	private volatile long endTime = 0;
	
	JJTask(final String name) {
		this.name = name;
	}

	protected abstract void run() throws Exception;
	
	abstract ScheduledExecutorService executor(ExecutorBundle executors);
	
	String name() {
		return name;
	}
	
	void enqueue() {
		enqueuedTime = System.currentTimeMillis();
	}
	
	void start() {
		startTime = System.currentTimeMillis();
	}
	
	void end() {
		endTime = System.currentTimeMillis();
	}
	
	boolean enqueued() {
		return enqueuedTime > 0;
	}

	boolean started() {
		return startTime > 0;
	}
	
	boolean finished() {
		return endTime > 0;
	}
	
	long timeInQueue() {
		return System.currentTimeMillis() - enqueuedTime;
	}
	
	long executionTime() {
		return endTime - startTime;
	}
	
	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(MAX_QUEUED_TIME - (System.currentTimeMillis() - enqueuedTime), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public int compareTo(Delayed o) {
		long x = getDelay(TimeUnit.MILLISECONDS);
		long y = o.getDelay(TimeUnit.MILLISECONDS);
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}
}
