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
	
	private static final ThreadLocal<JJRunnable> current = new ThreadLocal<JJRunnable>() {};

	private final ExecutionTrace trace;
	
	@Inject
	TaskCreator(
		final ExecutionTrace trace
	) {
		this.trace = trace;
	}
	
	public Runnable prepareTask(final JJRunnable task) {
		
		final boolean traceLog = !task.ignoreInExecutionTrace(); 
		if (traceLog) trace.preparingTask(current.get(), task);
		
		return new Runnable() {
			
			@Override
			public final void run() {
				try {
					current.set(task);
					if (traceLog) trace.startingTask(task);
					task.run();
					if (traceLog) trace.taskCompletedSuccessfully(task);
				} catch (OutOfMemoryError rethrow) {
					throw rethrow;
				} catch (Throwable t) {
					if (traceLog) trace.taskCompletedWithError(task, t);
				} finally {
					current.set(null);
				}
			}
			
			@Override
			public String toString() {
				return task.toString();
			}
		};
	}
	
	<T> RunnableFuture<T> newIOTask(final Runnable runnable, final T value) {
		return newTaskFor(runnable, value);
	}
	
	private <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
		
		return new FutureTask<T>(runnable, value) {
			@Override
			public void run() {
				try {
					System.err.println("starting task [" + runnable + "]");
					runnable.run();
				} finally {
					System.err.println("ending task [" + runnable + "]");
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
					System.err.println("starting task [" + runnable + "]");
					task.run();
				} finally {
					System.err.println("ending task [" + runnable + "]");
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
