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
package jj.execution;

import java.util.concurrent.TimeUnit;


/**
 * @author jason
 *
 */
public abstract class DelayedTask<T extends DelayedExecutor> extends JJTask {

	protected DelayedTask(String name) {
		super(name);
	}
	
	
	protected abstract T findExecutor(ExecutorFinder executors);

	protected long delay() {
		return 0;
	}
	

	protected final void addRunnableToExecutor(ExecutorFinder executors, Runnable runnable) {
		findExecutor(executors).submit(runnable, delay(), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * invoke this during the run if the task should be
	 * scheduled again
	 */
	protected final void repeat() {
		promise().then(this);
	}
}
