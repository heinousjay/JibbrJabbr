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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author jason
 *
 */
public class MockJJExecutor implements JJExecutor {

	public List<JJTask> tasks = new ArrayList<>();
	
	public void runUntilIdle() throws Exception {
		JJTask task = tasks.isEmpty() ? null : tasks.remove(0);
		while (task != null) {
			task.run();
			task = tasks.isEmpty() ? null : tasks.remove(0);
		}
	}
	
	public void runFirstTask() throws Exception {
		assert(!tasks.isEmpty());
		
		tasks.remove(0).run();
	}
	
	@Override
	public Future<Void> execute(final JJTask task) {
		tasks.add(task);
		
		return new Future<Void>() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public Void get() throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return null;
			}
		};
	}
	
	public boolean isScriptThread = false;
	
	@Override
	public boolean isScriptThread(){
		return isScriptThread;
	}

	@Override
	public boolean isScriptThreadFor(String baseName) {
		return isScriptThread;
	}
	
	public boolean isIOThread = false;

	@Override
	public boolean isIOThread() {
		return isIOThread;
	}

	@Override
	public int ioPoolSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
