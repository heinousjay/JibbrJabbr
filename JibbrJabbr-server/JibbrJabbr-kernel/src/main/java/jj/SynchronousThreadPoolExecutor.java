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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

/**
 * <p>
 * handles the synchronous tasks 
 * </p>
 * 
 * @author jason
 *
 */
public class SynchronousThreadPoolExecutor 
		extends JJThreadPoolExecutor
		implements RejectedExecutionHandler {
	
	private final class KernelTask<V> 
		extends FutureTask<V> {

		/**
		 * @param callable
		 */
		public KernelTask(Callable<V> callable) {
			super(callable);
		}
		
		/**
		 * 
		 */
		public KernelTask(Runnable runnable, V result) {
			super(runnable, result);
		}
		
		@Override
		protected void done() {
			logger.info(SynchronousTaskDone);
		}
		
	}
	
	private final LocLogger logger;
	
	private final MessageConveyor messageConveyor;
	
	private final AtomicInteger idSource = new AtomicInteger(1);
	
	private final ThreadGroup threadGroup = new ThreadGroup(SynchronousThreadPoolExecutor.class.getSimpleName());
	
	public SynchronousThreadPoolExecutor(
		final LocLogger logger,
		final MessageConveyor messageConveyor,
		final KernelSettings mainSettings
	) {
		super(
			logger,
			mainSettings.synchronousThreadCoreCount(), 
			mainSettings.synchronousThreadMaxCount(),
			mainSettings.synchronousThreadTimeOut(), SECONDS, 
			new LinkedBlockingQueue<Runnable>()
		);
		this.logger = logger;
		this.messageConveyor = messageConveyor;
		this.setRejectedExecutionHandler(this);
		logger.debug(ObjectInstantiated, SynchronousThreadPoolExecutor.class);
	}
	
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new KernelTask<T>(callable);
	}
	
	protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
		return new KernelTask<T>(runnable, value);
	};

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		logger.error(SynchronousTaskRejected);
	}
	
	String threadName() {
		return messageConveyor.getMessage(SynchronousThreadName,
			idSource.getAndIncrement(),
			new Date()
		);
	}
	
	ThreadGroup threadGroup() {
		return threadGroup;
	}
	
}