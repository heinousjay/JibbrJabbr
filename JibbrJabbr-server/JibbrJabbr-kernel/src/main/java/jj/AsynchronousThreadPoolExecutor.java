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
import static jj.KernelMessages.AsynchronousTaskRejected;
import static jj.KernelMessages.AsynchronousThreadName;
import static jj.KernelMessages.ObjectInstantiated;

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
 * Thread pool for handling asynchronous tasks.  This is largely an organizational concept.
 * Essentially no task that gets submitted into this pool is allowed to block.  I don't
 * think Java gives me a way to enforce that completely but one of the future tasks here
 * is to add a security manager that forbids manipulating files or the network if that is
 * possible to do on a per thread basis. If not, then classloader?  if not... well dammit.
 * </p>
 * 
 * <p>
 * Also have to verify netty works as I expect in this regard, or provide it a different
 * pool for network tasks.  
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
public class AsynchronousThreadPoolExecutor 
		extends JJThreadPoolExecutor 
		implements RejectedExecutionHandler {
	
	private final class AsynchronousPoolTask<V> extends FutureTask<V> {

		public AsynchronousPoolTask(Callable<V> callable) {
			super(callable);
		}
		
		public AsynchronousPoolTask(Runnable runnable, V result) {
			super(runnable, result);
		}
	}
	
	private final MessageConveyor messageConveyor;

	private final LocLogger logger;
	
	private final AtomicInteger idSource = new AtomicInteger(1);
	
	private final ThreadGroup threadGroup = new ThreadGroup(AsynchronousThreadPoolExecutor.class.getSimpleName());
	
	public AsynchronousThreadPoolExecutor(
		final LocLogger logger,
		final KernelSettings kernelSettings,
		final MessageConveyor messageConveyor) {
		super(
			logger,
			kernelSettings.asynchronousThreadCoreCount(),
			kernelSettings.asynchronousThreadMaxCount(),
			kernelSettings.asynchronousThreadTimeOut(), SECONDS,
			new LinkedBlockingQueue<Runnable>()
		);
		this.logger = logger;
		this.messageConveyor = messageConveyor;
		this.setRejectedExecutionHandler(this);
		
		logger.debug(ObjectInstantiated, AsynchronousThreadPoolExecutor.class);
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
		logger.error(AsynchronousTaskRejected);
	}

	@Override
	String threadName() {
		return messageConveyor.getMessage(AsynchronousThreadName,
			idSource.getAndIncrement(),
			new Date()
		);
	}

	@Override
	ThreadGroup threadGroup() {
		return threadGroup;
	}

}
