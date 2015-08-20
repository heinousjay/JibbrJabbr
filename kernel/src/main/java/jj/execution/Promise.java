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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simply a way to attach a task to be executed at the end of another task.
 *
 * @author jason
 *
 */
public class Promise {

	private TaskRunner taskRunner;
	
	private final AtomicBoolean done = new AtomicBoolean();
	
	private final AtomicReference<List<JJTask<?>>> next = new AtomicReference<>();
	
	Promise() {}
	
	Promise taskRunner(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
		return this;
	}

	/**
	 * <p>
	 * Ensures that the next task will get scheduled once the promised task
	 * is finished running, regardless of success or error.  no provision is
	 * planned to share any information, so tasks are expected to coordinate
	 * amongst themselves, unless some pattern becomes evident
	 * 
	 * <p>
	 * note that chaining calls currently means you are setting up tasks in sequence,
	 * so that<pre class="brush:java">
	 * execute(task1).then(task2).then(task3);
	 * </pre>
	 * means that task2 starts when task1 finishes, and task3 starts when task2 is done
	 * @param next
	 * @return
	 */
	public Promise then(final JJTask<?> task) {
		
		next().add(task);
		
		// if i'm considering this correctly - by allowing the task to get added to the list
		// and then checking for the latch to be broken, i've established a happens-before
		// relationship that guarantees that if i then execute all tasks in the list, it won't
		// double up
		if (done.get()) {
			List<JJTask<?>> tasks = next.getAndSet(null);
			if (tasks != null) {
				for (JJTask<?> t : tasks) {
					taskRunner.execute(t);
				}
			}
		}
		
		return task.promise();
	}
	
	private List<JJTask<?>> next() {
		if (next.get() == null) {
			next.compareAndSet(null, new ArrayList<JJTask<?>>());
		}
		return next.get();
	}
	
	List<JJTask<?>> done() {
		done.set(true);
		return next.getAndSet(null);
	}
}
