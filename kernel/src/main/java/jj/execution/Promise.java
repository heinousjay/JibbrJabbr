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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simply a way to attach a task to be executed at the end of another task.
 * 
 * Also provides an await method, which can only be called from one specific
 * place and will throw an assertion from anywhere else so DON'T CALL IT.
 * 
 * @author jason
 *
 */
public class Promise {

	private TaskRunner taskRunner;
	
	private final CountDownLatch done = new CountDownLatch(1);
	
	private final AtomicReference<List<JJTask>> next = new AtomicReference<>();
	
	Promise() {}
	
	Promise taskRunner(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
		return this;
	}

	/**
	 * Ensures that the next task will get schedules once the promised task
	 * is finished running, regardless of success or error.  no provision is
	 * planned to share any information, so tasks are expected to coordinate
	 * amongst themselves, unless some pattern becomes evident
	 * 
	 * note that chaining calls currently means you are setting up tasks in sequence,
	 * so that
	 * execute(task1).then(task2).then(task3);
	 * means that task2 starts when task1 finishes, and task3 starts when task2 is done
	 * @param next
	 * @return
	 */
	public Promise then(final JJTask task) {
		
		next().add(task);
		
		// if i'm considering this correctly - by allowing the task to get added to the list
		// and then checking for the latch to be broken, i've established a happens-before
		// relationship that guarantees that if i then execute all tasks in the list, it won't
		// double up
		if (done.getCount() == 0) {
			List<JJTask> tasks = next.getAndSet(null);
			if (tasks != null) {
				for (JJTask t : tasks) {
					taskRunner.execute(t);
				}
			}
		}
		
		return task.promise();
	}
	
	private List<JJTask> next() {
		if (next.get() == null) {
			next.compareAndSet(null, new ArrayList<JJTask>());
		}
		return next.get();
	}
	
	/**
	 * Waits for the promised task completion for 1 second.  Only callable from one place in this system,
	 * which i am considering refactoring out of existence entirely
	 * @throws TimeoutException
	 */
	public void await() throws TimeoutException, InterruptedException {
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		assert stackTrace[1].getClassName().equals("jj.configuration.ConfigurationScriptPreloader");
		assert stackTrace[1].getMethodName().equals("start");
		
		// okay, fine... but only for you!
		done.await(1, TimeUnit.SECONDS);
	}
	
	List<JJTask> done() {
		done.countDown();
		return next.getAndSet(null);
	}
}
