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
package jj.client;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configures an executor for the client subsystem
 * 
 * @author jason
 *
 */
class ClientExecutor extends ScheduledThreadPoolExecutor {
	
	// reaper the +
	// half the processors
	public static final int WORKER_COUNT = 1 + (int)(Runtime.getRuntime().availableProcessors() * 0.5);
	
	private static final ThreadFactory threadFactory = new ThreadFactory() {
		
		private final AtomicInteger id = new AtomicInteger();
		
		@Override
		public Thread newThread(final Runnable r) {
			final String name = String.format(
				"JibbrJabbr Client I/O Handler %d", 
				id.incrementAndGet()
			);
			Thread thread = new Thread(r, name);
			thread.setDaemon(true);
			return thread;
		}
	};
	
	private static final RejectedExecutionHandler rejectedExecutionHandler =
		new RejectedExecutionHandler() {
			
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				System.err.println("ran out of room for an client task.  OOM error coming shortly!");
			}
		};
	
	ClientExecutor() {
		super(
			1, 
			threadFactory,
			rejectedExecutionHandler
		);
		this.setCorePoolSize(WORKER_COUNT);
		this.setMaximumPoolSize(WORKER_COUNT);
	}
}
