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
package jj.script;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.CurrentTask;
import jj.execution.TaskRunner;
import jj.util.SecureRandomHelper;

/**
 * @author jason
 *
 */
@Singleton
class ContinuationPendingCache {
	
	private final TaskRunner taskRunner;
	
	private final CurrentTask currentTask;
	
	@Inject
	ContinuationPendingCache(final TaskRunner taskRunner, final CurrentTask currentTask) {
		this.taskRunner = taskRunner;
		this.currentTask = currentTask;
	}
	
	private final ScriptTask<ScriptEnvironment> reserved = 
		new ScriptTask<ScriptEnvironment>("", null) {
		
			@Override
			protected void begin() throws Exception {
				throw new AssertionError("reserved sentinel was run!");
			}
		};
		
	private final ScriptTask<ScriptEnvironment> alreadyResumed =
		new ScriptTask<ScriptEnvironment>("", null) {
			
			@Override
			protected void begin() throws Exception {
				throw new AssertionError("already resumed sentinel was run!");
			}
		};
	
	/**
	 * tasks awaiting resumption.
	 */
	private final ConcurrentMap<String, ScriptTask<?>> resumableTasks = new ConcurrentHashMap<>();
	
	/**
	 * bulk removable of pending tasks
	 * @param keys A collection of {@link PendingKey}s to remove
	 */
	void removePendingTasks(Collection<PendingKey> keys) {
		keys.forEach(key -> {
			resumableTasks.remove(key.id());
		});
	}
	
	String uniqueID() {
		// wow.  a do...while!
		String result;
		do {
			result = Integer.toHexString(SecureRandomHelper.nextInt());
		} while (resumableTasks.putIfAbsent(result, reserved) != null);
		
		return result;
	}
	
	void storeForContinuation(final ScriptTask<?> task) {
		PendingKey pendingKey = task.pendingKey();
		
		if (pendingKey != null) {
			if (!resumableTasks.replace(pendingKey.id(), reserved, task) &&
				!resumableTasks.remove(pendingKey.id(), alreadyResumed)
			) {
				throw new AssertionError("pending key being stored was not reserved or is already in use!");
			}
		}
	}
	
	void resume(final PendingKey pendingKey, final Object result) {
		assert pendingKey != null : "attempting to resume without a pendingKey";
		
		ScriptTask<?> task = resumableTasks.remove(pendingKey.id());
		// probably not an assertion in the long run - people will at some point be sending bullshit results at this thing and
		// we will just ignore them.  but that will be when one can do things like run with kernel assertions off :D
		// so NOT YET
		assert task != null : "asked to resume a nonexistent task";
		if (task != reserved) {
			task.resumeWith(result);
		} else if (currentTask.currentIs(ScriptTask.class)) { // it resumed immediately, via some stroke of luck
			resumableTasks.putIfAbsent(pendingKey.id(), alreadyResumed);
			task = currentTask.currentAs(ScriptTask.class);   // so reschedule the current task to run again
			task.resumeWith(result);
		} else {
			throw new AssertionError("asked to resume an unstored key from a non-ScriptTask. weird error, weird message!");
		}
		
		taskRunner.execute(task);
	}
}
