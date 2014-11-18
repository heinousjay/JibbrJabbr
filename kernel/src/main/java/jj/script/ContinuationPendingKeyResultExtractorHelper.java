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

import java.util.HashMap;
import java.util.Map;

import jj.execution.CurrentTask;
import jj.execution.JJTask;
import jj.execution.Promise;
import jj.execution.TaskRunner;

/**
 * A helper for testing the script system.
 * 
 * @author jason
 *
 */
class ContinuationPendingKeyResultExtractorHelper extends ContinuationPendingCache {

	static Map<ContinuationPendingKey, Object> RESULT_MAP = new HashMap<>();
	
	/**
	 * @param taskRunner
	 */
	ContinuationPendingKeyResultExtractorHelper() {
		super(new TaskRunner() { // cause really, it shouldn't happen
			
			@Override
			public Promise execute(JJTask task) {
				throw new AssertionError("how did this get called?");
			}
		}, new CurrentTask());
	}

	@Override
	void resume(ContinuationPendingKey pendingKey, Object result) {
		RESULT_MAP.put(pendingKey, result);
	}
	
}
