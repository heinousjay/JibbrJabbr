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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

abstract class JJThreadPoolExecutor 
		extends ThreadPoolExecutor 
		implements ThreadFactory {

	JJThreadPoolExecutor(
		final int corePoolSize,
		final int maximumPoolSize,
		final long keepAliveTime,
		final TimeUnit unit,
		final BlockingQueue<Runnable> workQueue
	) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.setThreadFactory(this);
	}
	
	abstract String threadName();
	
	abstract ThreadGroup threadGroup();
	
	@Override
	public Thread newThread(final Runnable runnable) {
		
		return new Thread(threadGroup(), runnable, threadName());
	}
}
