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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jj.util.Clock;

/**
 * <p>
 * supports a simple "run every x time units" and "run after x time units" approach to
 * scheduling.  the java stuff is nice, but works too hard for my needs and makes some
 * other things too sloppy
 * 
 * <p>
 * If the runnable submitted to this executor throws an exception it will be silently
 * discarded, so make sure you do all your handling below this. Presuming you use this
 * executor through the {@link TaskRunner} this is handled for you.  If you use this
 * executor in an ad-hoc fashion, then it is on you.
 * @author jason
 *
 */
public abstract class DelayedExecutor extends ThreadPoolExecutor {
	
	public class CancelKey {
		
		private final WeakReference<DelayedRunnable> runnable;
		
		private CancelKey(DelayedRunnable runnable) {
			this.runnable = new WeakReference<>(runnable);
		}
		
		public void cancel() {
			DelayedRunnable r = runnable.get();
			if (r != null) {
				r.canceled.set(true);
				delayedTasks.remove(r);
			}
		}
	}
	
	private class DelayedRunnable implements Runnable, Delayed {
		
		private final Runnable runnable;
		
		private final long delay;
		
		private final TimeUnit timeUnit;
		
		private final long start = clock.time();
		
		private final AtomicBoolean canceled = new AtomicBoolean();
		
		protected DelayedRunnable(Runnable runnable, long delay, TimeUnit timeUnit) {
			this.runnable = runnable;
			this.delay = delay;
			this.timeUnit = timeUnit;
		}

		@Override
		public void run() {
			if (!canceled.get()) { 
				runnable.run();
			}
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
	
	protected final Clock clock;
	protected final JJThreadFactory threadFactory;
	
	private final DelayQueue<DelayedRunnable> delayedTasks = new DelayQueue<>();
	
	private final Runnable scheduler = new Runnable() {
		
		@Override
		public void run() {
			Thread.currentThread().setName(schedulerThreadName());
			try {
				while (true) {
					DelayedRunnable runnable = delayedTasks.take();
					if (asynchronousScheduling()) {
						submit(runnable);
					} else {
						try { runnable.run(); } catch (Throwable t) {}
					}
				}
			} catch (InterruptedException e) {
				
			}
		}
	};
	
	protected DelayedExecutor(
		final Clock clock,
		final int corePoolSize,
		final int maxPoolSize,
		final int timeOut,
		final TimeUnit timeOutUnit,
		final BlockingQueue<Runnable> workQueue,
		final JJThreadFactory threadFactory,
		final JJRejectedExecutionHandler handler
	) {
		super(
			corePoolSize,
			maxPoolSize,
			timeOut,
			timeOutUnit,  
			workQueue,
			threadFactory,
			handler
		);
		
		this.clock = clock;
		this.threadFactory = threadFactory;
		
		submit(scheduler);
	}
	
	/**
	 * Name your scheduler thread something intelligent for easier debugging
	 * @return
	 */
	protected abstract String schedulerThreadName();
	
	/**
	 * By default, this executor will run a scheduler thread and dispatch the actual tasks
	 * back into the executor.  If you want a single-thread executor, override this and return
	 * false, and the tasks will execute inline.
	 * @return
	 */
	protected boolean asynchronousScheduling() {
		return true;
	}
	
	public CancelKey submit(Runnable runnable, long delay, TimeUnit timeUnit) {
		DelayedRunnable delayed = new DelayedRunnable(runnable, delay, timeUnit);
		delayedTasks.add(delayed);
		return new CancelKey(delayed);
	}

}