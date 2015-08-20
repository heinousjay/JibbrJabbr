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
package jj.resource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jj.execution.JJTask;

/**
 * A task for loading resources.  In most cases, try to use 
 * {@link ResourceLoader} first.
 * 
 * @author jason
 *
 */
public abstract class ResourceTask extends JJTask<ResourceExecutor> {
	
	private final CountDownLatch completionLatch = new CountDownLatch(1);
	
	protected ResourceTask(String name) {
		super(name);
	}
	
	@Override
	protected final void addRunnableToExecutor(ExecutorFinder executors, final Runnable runnable) {
		executors.ofType(ResourceExecutor.class).submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					completionLatch.countDown();
				}
			}
		});
	}

	/**
	 * used to synchronize resource creation across multiple requests
	 */
	void await(long time, TimeUnit unit) {
		try {
			completionLatch.await(time, unit);
		} catch (Exception e) {
			throw new AssertionError(e); // good enough for now
		}
	}
}
