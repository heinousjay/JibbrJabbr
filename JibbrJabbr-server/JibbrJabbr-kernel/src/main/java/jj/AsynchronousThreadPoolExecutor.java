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

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.KernelMessages.*;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.cal10n.IMessageConveyor;
/**
 * <p>
 * Thread pool for handling asynchronous tasks.  This is largely an organizational concept.
 * Essentially no task that gets submitted into this pool is allowed to block.
 * </p>
 * 
 * <p>
 * The pool is configured with an unbounded queue for incoming tasks, since it should in
 * principle be able to burn through pending tasks relatively quickly if it truly never blocks
 * </p>
 * 
 * @author Jason Miller
 *
 */
class AsynchronousThreadPoolExecutor 
		extends ThreadPoolExecutor 
		implements AsyncThreadPool, RejectedExecutionHandler, ThreadFactory {
	
	private final class AsynchronousPoolTask<V> extends FutureTask<V> {

		public AsynchronousPoolTask(Callable<V> callable) {
			super(callable);
		}
		
		public AsynchronousPoolTask(Runnable runnable, V result) {
			super(runnable, result);
		}
	}
	
	private final IMessageConveyor messageConveyor;
	
	private final AtomicInteger idSource = new AtomicInteger(1);
	
	private final ThreadGroup threadGroup = new ThreadGroup(AsynchronousThreadPoolExecutor.class.getSimpleName());
	
	public AsynchronousThreadPoolExecutor(
		final IMessageConveyor messageConveyor,
		final KernelSettings kernelSettings,
		final EventMediationService ems
	) {
		super(
			kernelSettings.asynchronousThreadCoreCount(),
			kernelSettings.asynchronousThreadMaxCount(),
			kernelSettings.asynchronousThreadTimeOut(), SECONDS,
			new LinkedBlockingQueue<Runnable>()
		);
		this.messageConveyor = messageConveyor;
		this.setRejectedExecutionHandler(this);
		this.setThreadFactory(this);
		ems.register(this);
	}
	
	public void control(KernelControl control) {
		// hit them brakes baby
		if (control == KernelControl.Dispose) {
			this.shutdownNow();
		}
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new AsynchronousPoolTask<T>(callable);
	}
	
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new AsynchronousPoolTask<T>(runnable, value);
	};
	
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		// can't happen with the current configuration
		// need to log it
	}

	private String threadName() {
		return messageConveyor.getMessage(AsynchronousThreadName,
			idSource.getAndIncrement(),
			new Date()
		);
	}

	@Override
	public Thread newThread(Runnable runnable) {
		return new Thread(threadGroup, runnable, threadName());
	}

}
