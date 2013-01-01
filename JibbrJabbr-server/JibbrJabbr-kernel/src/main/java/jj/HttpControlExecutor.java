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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jason
 *
 */
public class HttpControlExecutor extends ScheduledThreadPoolExecutor {
	
	public boolean isHttpControlThread() {
		return flag.get() != null;
	}
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();

	// just one.  maybe we can play with expanding this? but i doubt we
	// need to.
	public static final int WORKER_COUNT = 1;
	
	private static final ThreadFactory threadFactory = new ThreadFactory() {
		
		@Override
		public Thread newThread(final Runnable r) {
			return new Thread(
				new Runnable() {
					
					@Override
					public void run() {
						flag.set(true);
						r.run();
					}
				}
				, "Webbit HTTP control thread"
			);
		}
	};
	
	private static final RejectedExecutionHandler rejectedExecutionHandler =
		new RejectedExecutionHandler() {
			
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				// well.. whatever.  we're about to die bb yeah
				System.err.println("ran out of room for an http task.  OOM error coming shortly!");
			}
		};
		
	/**
	 * 
	 */
	public HttpControlExecutor() {
		super(WORKER_COUNT, threadFactory, rejectedExecutionHandler);
		setMaximumPoolSize(1);
		setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		setRemoveOnCancelPolicy(true);
	}

}
