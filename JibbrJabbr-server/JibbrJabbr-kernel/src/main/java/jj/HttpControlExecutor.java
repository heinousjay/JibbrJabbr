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

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class HttpControlExecutor extends ScheduledThreadPoolExecutor implements JJServerListener {
	
	public boolean isHttpControlThread() {
		return flag.get() != null;
	}
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();

	// just one.  maybe we can play with expanding this? but i doubt we
	// need to. and i believe i read somewhere that there is a handler in
	// webbit that uses a hashmap to track connections on this thread, so
	// it's clearly designed to only support 1
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
				System.err.println("ran out of room for an http control task.  OOM error coming shortly!");
				System.err.println(Runtime.getRuntime().maxMemory());
				System.err.println(Runtime.getRuntime().totalMemory());
				System.err.println(Runtime.getRuntime().freeMemory());
			}
		};
		
	@Inject
	public HttpControlExecutor() {
		super(WORKER_COUNT, threadFactory, rejectedExecutionHandler);
		setMaximumPoolSize(1);
		setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		setRemoveOnCancelPolicy(true);
	}

	@Override
	public void start() throws Exception {
		// nothing to do
	}

	@Override
	public void stop() {
		shutdownNow();
	}

}
