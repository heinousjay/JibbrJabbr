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
package jj;

import java.math.BigDecimal;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class TaskCreator {
	
	final class JJTask implements Runnable {
		
		final JJRunnable parent;
		final JJRunnable runnable;
		
		final long creationTime = System.currentTimeMillis(); 
		
		private final boolean traceLog;
		
		private JJTask(final JJRunnable runnable) {
			this.parent = current.get();
			this.runnable = runnable;
			
			traceLog = !runnable.ignoreInExecutionTrace(); 
			if (traceLog) trace.preparingTask(this);
		}
		
		private volatile long startNanos;
		private volatile long endNanos;
		private volatile long endTime;
		
		@Override
		public final void run() {

			try {
				startNanos = System.nanoTime();
				current.set(runnable);
				if (traceLog) trace.startingTask(this);
				runnable.run();
				if (traceLog) trace.taskCompletedSuccessfully(this);
			} catch (OutOfMemoryError rethrow) {
				throw rethrow;
			} catch (Throwable t) {
				if (traceLog) trace.taskCompletedWithError(this, t);
			} finally {
				current.set(null);
				endNanos = System.nanoTime();
				endTime = System.currentTimeMillis();
			}
		}
		
		public boolean isCompleted() {
			return endTime != 0;
		}
		
		public String executionTime() {
			return endNanos == 0 ? "not finished" : BigDecimal.valueOf(endNanos - startNanos, 6).toString() + " milliseconds";
		}
		
		@Override
		public String toString() {
			return new StringBuilder(runnable.toString())
				.append(" started from ")
				.append(parent)
				.append(" took ")
				.append(executionTime())
				.toString();
		}
	}
	
	private static final ThreadLocal<JJRunnable> current = new ThreadLocal<JJRunnable>() {};

	private final ExecutionTrace trace;
	
	@Inject
	TaskCreator(
		final ExecutionTrace trace
	) {
		this.trace = trace;
	}
	
	public Runnable prepareTask(final JJRunnable runnable) {
		
		return new JJTask(runnable);
	}
	
	<T> RunnableFuture<T> newIOTask(final Runnable runnable, final T value) {
		return newTaskFor(runnable, value);
	}
	
	private <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
		
		return new FutureTask<T>(runnable, value) {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
				}
			}
		};
	}
	
	<V> RunnableScheduledFuture<V> newClientTask(final Runnable runnable, final RunnableScheduledFuture<V> task) {
		return prepareTask(runnable, task);
	}
	
	<V> RunnableScheduledFuture<V> newHttpTask(final Runnable runnable, final RunnableScheduledFuture<V> task) {
		return prepareTask(runnable, task);
	}
	
	<V> RunnableScheduledFuture<V> newScriptTask(final Runnable runnable, final RunnableScheduledFuture<V> task) {
		return prepareTask(runnable, task);
	}
	
	private <V> RunnableScheduledFuture<V> prepareTask(final Runnable runnable, final RunnableScheduledFuture<V> task) {
		return new RunnableScheduledFuture<V>() {

			@Override
			public void run() {
				try {
					task.run();
				} finally {
				}
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return task.cancel(mayInterruptIfRunning);
			}

			@Override
			public boolean isCancelled() {
				return task.isCancelled();
			}

			@Override
			public boolean isDone() {
				return task.isDone();
			}

			@Override
			public V get() throws InterruptedException, ExecutionException {
				return task.get();
			}

			@Override
			public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return task.get(timeout, unit);
			}

			@Override
			public long getDelay(TimeUnit unit) {
				return task.getDelay(unit);
			}

			@Override
			public int compareTo(Delayed o) {
				return task.compareTo(o);
			}

			@Override
			public boolean isPeriodic() {
				return task.isPeriodic();
			}
			
		};
	}
}
