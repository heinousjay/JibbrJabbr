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

import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.SecureRandomHelper;
import jj.execution.JJTask;
import jj.execution.ResumableTask;
import jj.execution.TaskRunner;

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
	
	/**
	 * tasks awaiting resumption. can this be stored per executor somehow?
	 */
	private final ConcurrentMap<String, ResumableTask> resumableTasks = PlatformDependent.newConcurrentHashMap();
	
	String uniqueID() {
		// wow.  a do...while!
		String result;
		do {
			result = Integer.toHexString(SecureRandomHelper.nextInt());
		} while (resumableTasks.containsKey(result));
		
		return result;
	}
	
	void storeIfResumable(final JJTask task) {
		if (task instanceof ResumableTask) {
			ResumableTask resumable = (ResumableTask)task;
			ContinuationPendingKey pendingKey = resumable.pendingKey();
			
			if (pendingKey != null) {
				if (resumableTasks.putIfAbsent(pendingKey.id(), resumable) != null) {
					throw new AssertionError("pending key is being shared!");
				}
			}
		}
	}
	
	public void resume(final ContinuationPendingKey pendingKey, final Object result) {
		assert pendingKey != null : "attempting to resume without a pendingKey";
		
		ResumableTask task = resumableTasks.remove(pendingKey.id());
		// probably not an assertion in the long run - people will at some point be sending bullshit results at this thing and
		// we will just ignore them.  but that will be when one can do things like run with kernel assertions off :D
		// so NOT YET
		assert task != null : "asked to resume a nonexistent task";
		task.resumeWith(result);
		
		taskRunner.execute(task);
	}
}
