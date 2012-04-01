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

import static java.util.Calendar.YEAR;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.cal10n.LocLogger;

/**
 * <p>
 * Dedicated pool for decoding incoming HTTP requests
 * and handshaking WebSocket connections.  
 * </p>
 * 
 * @author Jason Miller
 *
 */
public class HttpThreadPoolExecutor 
		extends JJThreadPoolExecutor 
		implements RejectedExecutionHandler {
	
	private final class HttpTask<V> extends FutureTask<V> {

		public HttpTask(Callable<V> callable) {
			super(callable);
		}
		
		public HttpTask(Runnable runnable, V result) {
			super(runnable, result);
		}
		
	}
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	private final LocLogger logger;
	
	private final AtomicInteger idSource = new AtomicInteger(1);
	
	private final ThreadGroup threadGroup = new ThreadGroup(HttpThreadPoolExecutor.class.getSimpleName());
	
	public HttpThreadPoolExecutor(final LocLogger logger, final KernelSettings kernelSettings) {
		super(
			logger,
			kernelSettings.asynchronousThreadCoreCount(),
			kernelSettings.asynchronousThreadMaxCount(),
			kernelSettings.asynchronousThreadTimeOut(), SECONDS,
			new LinkedBlockingQueue<Runnable>()
		);
		this.logger = logger;
		this.setRejectedExecutionHandler(this);
		
		logger.debug("Instantiating {}", HttpThreadPoolExecutor.class);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new HttpTask<T>(callable);
	}
	
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new HttpTask<T>(runnable, value);
	};
	
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		// can't happen with the current configuration
		logger.warn("dropping connections");
	}

	@Override
	String threadName() {
		Calendar now = Calendar.getInstance(UTC);
		return String.format("HTTP thread %d (%d-%d-%d %d:%d:%dUTC)",
			idSource.getAndIncrement(),
			now.get(YEAR),
			now.get(MONTH) + 1,
			now.get(DAY_OF_MONTH),
			now.get(HOUR),
			now.get(MINUTE),
			now.get(SECOND)
		);
	}

	@Override
	ThreadGroup threadGroup() {
		return threadGroup;
	}

}
