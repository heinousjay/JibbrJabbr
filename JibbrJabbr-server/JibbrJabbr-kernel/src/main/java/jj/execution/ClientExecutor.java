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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerListener;


/**
 * Configures an executor for the client subsystem
 * 
 * @author jason
 *
 */
@Singleton
public class ClientExecutor extends ScheduledThreadPoolExecutor implements JJServerListener {
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();
	
	public static boolean isClientThread() {
		return (Boolean.TRUE == flag.get());
	}
	
	// reaper +
	// half the processors
	public static final int WORKER_COUNT = 1 + (int)(Runtime.getRuntime().availableProcessors() * 0.5);
		
	@Inject
	ClientExecutor(
		final UncaughtExceptionHandler uncaughtExceptionHandler
	) {
		super(
			1, 
			new ThreadFactory() {
				
				private final AtomicInteger id = new AtomicInteger();
				
				@Override
				public Thread newThread(final Runnable r) {
					final String name = String.format(
						"JibbrJabbr Http Client I/O Handler %d", 
						id.incrementAndGet()
					);
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							flag.set(Boolean.TRUE);
							r.run();
						}
					}, name);
					thread.setDaemon(true);
					thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
					return thread;
				}
			},
			new RejectedExecutionHandler() {
				
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					System.err.println("ran out of room for an client task.  OOM error coming shortly!");
				}
			}
		);
		this.setCorePoolSize(WORKER_COUNT);
		this.setMaximumPoolSize(WORKER_COUNT);
	}
	
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(
		final Runnable runnable,
		final RunnableScheduledFuture<V> task
	) {
		return new JJScheduledTask<>(runnable, task);
	}
	
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(
		final Callable<V> callable,
		final RunnableScheduledFuture<V> task
	) {
		System.err.println("something asked for a callable");
		new Exception().printStackTrace();
		return task;
	}

	@Override
	public void start() throws Exception {
		// nothing
	}

	@Override
	public void stop() {
		shutdownNow();
	}
}
