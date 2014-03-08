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

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Clock;
import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;

/**
 * Special internal executor for the various tasks the server needs done.  supports
 * a simple "run every x time units" and "run after x time units" approach to
 * scheduling.  the java stuff is nice, but works too hard for my needs and makes some
 * other things too sloppy
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class ServerExecutor extends ThreadPoolExecutor {
	
	private final Clock clock;
	
	private final JJThreadFactory threadFactory;
	
	private class DelayedRunnable implements Runnable, Delayed {
		
		private final Runnable runnable;
		
		private final long delay;
		
		private final TimeUnit timeUnit;
		
		private final long start = clock.time();
		
		protected DelayedRunnable(Runnable runnable, long delay, TimeUnit timeUnit) {
			this.runnable = runnable;
			this.delay = delay;
			this.timeUnit = timeUnit;
		}

		@Override
		public void run() {
			runnable.run();
		}

		@Override
		public int compareTo(Delayed o) {
			long x = getDelay(TimeUnit.MILLISECONDS);
			long y = o.getDelay(TimeUnit.MILLISECONDS);
			return (x < y) ? -1 : ((x == y) ? 0 : 1);
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert((start + delay) - clock.time(), timeUnit);
		}
	}
	
	private final DelayQueue<DelayedRunnable> delayedTasks = new DelayQueue<>();
	
	private final Runnable scheduler = new Runnable() {
		
		@Override
		public void run() {
			Thread.currentThread().setName(Thread.currentThread().getName() + " - ServerExecutor scheduler");
			try {
				while (true) {
					DelayedRunnable runnable = delayedTasks.take();
					submit(runnable);
				}
			} catch (InterruptedException e) {
				
			}
		}
	};

	@Inject
	ServerExecutor(
		final Clock clock,
		final JJThreadFactory threadFactory,
		final JJRejectedExecutionHandler handler
	) {
		// so basically, we're looking at keeping a thread around for timer tasks and
		// a spare for kicks
		super(
			2, // core threads, which we will always keep alive.  effectively one since the first thing we do is start a scheduler
			Integer.MAX_VALUE, // never run out of threads for server tasks until we exhaust the machine
			20, TimeUnit.SECONDS, // don't keep threads around too long if they aren't kept busy.  
			new SynchronousQueue<Runnable>(), // hand off tasks immediately for execution
			threadFactory.namePattern("JibbrJabbr Server Thread %d"),
			handler
		);
		this.clock = clock;
		this.threadFactory = threadFactory;
		
		submit(scheduler);
	}

	@Listener
	public void stop(ServerStopping event) {
		shutdownNow();
	}
	
	// this needs to return a cancel key
	void submit(Runnable runnable, long delay, TimeUnit timeUnit) {
		DelayedRunnable delayed = new DelayedRunnable(runnable, delay, timeUnit);
		delayedTasks.add(delayed);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * @return
	 */
	boolean isServerThread() {
		return threadFactory.in();
	}
	
}
