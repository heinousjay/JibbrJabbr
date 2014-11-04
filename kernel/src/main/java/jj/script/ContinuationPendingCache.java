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

import jj.execution.TaskRunner;
import jj.util.SecureRandomHelper;

/**
 * @author jason
 *
 */
@Singleton
class ContinuationPendingCache {
	
	private final TaskRunner taskRunner;
	
	@Inject
	ContinuationPendingCache(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
	}
	
	private final ScriptTask<ScriptEnvironment> sentinel = 
		new ScriptTask<ScriptEnvironment>("", null, null) {
		
			@Override
			protected void begin() throws Exception {
				// will never be run
			}
		};
	
	/**
	 * tasks awaiting resumption.
	 */
	private final ConcurrentMap<String, ScriptTask<?>> resumableTasks = new ConcurrentHashMap<>();
	
	/**
	 * bulk removable of pending tasks
	 * @param keys A collection of {@link ContinuationPendingKey}s to remove
	 */
	void removePendingTasks(Collection<ContinuationPendingKey> keys) {
		keys.forEach(key -> {
			resumableTasks.remove(key.id());
		});
	}
	
	String uniqueID() {
		// wow.  a do...while!
		String result;
		do {
			result = Long.toHexString(SecureRandomHelper.nextLong());
		} while (resumableTasks.putIfAbsent(result, sentinel) != null);
		
		return result;
	}
	
	void storeForContinuation(final ScriptTask<?> task) {
		ContinuationPendingKey pendingKey = task.pendingKey();
		
		if (pendingKey != null) {
			if (!resumableTasks.replace(pendingKey.id(), sentinel, task)) {
				throw new AssertionError("pending key being stored was not reserved or is already in use!");
			}
		}
	}
	
	void resume(final ContinuationPendingKey pendingKey, final Object result) {
		assert pendingKey != null : "attempting to resume without a pendingKey";
		
		ScriptTask<?> task = resumableTasks.remove(pendingKey.id());
		// probably not an assertion in the long run - people will at some point be sending bullshit results at this thing and
		// we will just ignore them.  but that will be when one can do things like run with kernel assertions off :D
		// so NOT YET
		assert task != null : "asked to resume a nonexistent task";
		assert task != sentinel : "key reserved was never stored";
		task.resumeWith(result);
		
		taskRunner.execute(task);
	}
}
