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

/**
 * @author jason
 *
 */
public class MockTaskRunner implements TaskRunner {

	public List<JJTask> tasks = new ArrayList<>();
	
	public void runUntilIdle() throws Exception {
		JJTask task = tasks.isEmpty() ? null : tasks.remove(0);
		while (task != null) {
			task.run();
			task = tasks.isEmpty() ? null : tasks.remove(0);
		}
	}

	/**
	 * @return
	 */
	public JJTask firstTask() {
		return tasks.get(0);
	}
	
	public void runFirstTask() throws Exception {
		assert(!tasks.isEmpty());
		
		tasks.remove(0).run();
	}
	
	
	@Override
	public Promise execute(final JJTask task) {
		tasks.add(task);
		
		return task.promise().taskRunner(this);
	}
	
	public boolean isIOThread = false;

	@Override
	public boolean isIOThread() {
		return isIOThread;
	}
}
