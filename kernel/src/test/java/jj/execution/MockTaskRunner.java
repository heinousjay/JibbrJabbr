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

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import jj.execution.DelayedExecutor.CancelKey;

/**
 * @author jason
 *
 */
@Singleton
public class MockTaskRunner implements TaskRunner {

	public List<JJTask<?>> tasks = new ArrayList<>();
	
	public void runUntilIdle() throws Exception {
		JJTask<?> task = tasks.isEmpty() ? null : tasks.remove(0);
		while (task != null) {
			task.run();
			task = tasks.isEmpty() ? null : tasks.remove(0);
		}
	}

	public JJTask<?> firstTask() {
		return tasks.get(0);
	}
	
	public DelayedTask<?> firstDelayedTask() {
		return (DelayedTask<?>)tasks.get(0);
	}
	
	public JJTask<?> runFirstTask() throws Exception {
		assert(!tasks.isEmpty());
		
		JJTask<?> task = tasks.remove(0);
		try {
			task.run();
		} catch (Exception cause) {
			if (!task.errored(cause)) {
				throw cause;
			}
		}
		
		return task;
	}
	
	public Thread runFirstTaskInDaemon() throws Exception {
		assert(!tasks.isEmpty());
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					runFirstTask();
				} catch (InterruptedException ie) { // eat this, we are shutting it down
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();
		
		return t;
	}
	
	public CancelKey cancelKey;
	
	@Override
	public Promise execute(final JJTask<?> task) {
		if (cancelKey != null && task instanceof DelayedTask<?>) {
			DelayedTask<?> dTask = (DelayedTask<?>)task;
			dTask.cancelKey = cancelKey;
			cancelKey = null;
		}
		tasks.add(task);
		
		Promise result;
		
		if (mockingDetails(task).isMock()) {
			result = mock(Promise.class);
			when(result.then(any(JJTask.class))).then(new Answer<Promise>() {

				@Override
				public Promise answer(InvocationOnMock invocation) throws Throwable {
					((JJTask<?>)invocation.getArguments()[0]).run();
					return result;
				}
				
			});
		} else {
			result = task.promise().taskRunner(this);
		}
		
		return result;
	}
	
	public long firstTaskDelay() {
		assert !tasks.isEmpty() && tasks.get(0) instanceof DelayedTask : "no DelayedTask at index 0";
		
		return delay(tasks.get(0));
	}
	
	public long delay(JJTask<?> task) {
		assert task instanceof DelayedTask : "not a DelayedTask";
	
		return ((DelayedTask<?>)task).delay();
	}
	
	public boolean taskWillRepeat(JJTask<?> task) {
		assert task instanceof DelayedTask : "not a DelayedTask";
		
		return ((DelayedTask<?>)task).willRepeat();
	}
}
