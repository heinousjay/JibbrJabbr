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

import static jj.KernelMessages.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.cal10n.LocLogger;

abstract class JJThreadPoolExecutor 
		extends ThreadPoolExecutor 
		implements ThreadFactory {
	
	private final LocLogger logger;

	JJThreadPoolExecutor(
		final LocLogger logger,
		final int corePoolSize,
		final int maximumPoolSize,
		final long keepAliveTime,
		final TimeUnit unit,
		final BlockingQueue<Runnable> workQueue
	) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.logger = logger;
		this.setThreadFactory(this);
	}
	
	abstract String threadName();
	
	abstract ThreadGroup threadGroup();
	
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		logger.debug(JJTaskStarting, r.getClass());
		super.beforeExecute(t, r);
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		
		if (t != null) {
			logger.error(JJTaskEndedWithException, r, t);
		} else {
			logger.debug(JJTaskEnded, r.getClass());
		}
		super.afterExecute(r, t);
	}
	
	@Override
	public Thread newThread(final Runnable inner) {
		
		String name = threadName();
			
		
		logger.debug(JJThreadInitializing, name, Thread.currentThread().getName());
		
		Thread thread = new Thread(
			threadGroup(), 
			new Runnable() {
				
				@Override
				public void run() {
					logger.trace(JJThreadStarting);
					inner.run();
					logger.trace(JJThreadExiting);
				}
			}, 
			name
		);
		
		return thread;
	}
}
