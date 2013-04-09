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
package jj;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class TaskCreator {
	
	private static final ThreadLocal<JJRunnable> current = new ThreadLocal<JJRunnable>() {};

	private final ExecutionTrace trace;
	
	@Inject
	TaskCreator(
		final ExecutionTrace trace
	) {
		this.trace = trace;
	}
	
	public Runnable prepareTask(final JJRunnable task) {
		
		final boolean traceLog = !task.ignoreInExecutionTrace(); 
		if (traceLog) trace.preparingTask(current.get(), task);
		
		return new Runnable() {
			
			@Override
			public final void run() {
				try {
					current.set(task);
					if (traceLog) trace.startingTask(task);
					task.run();
					if (traceLog) trace.taskCompletedSuccessfully(task);
				} catch (OutOfMemoryError rethrow) {
					throw rethrow;
				} catch (Throwable t) {
					if (traceLog) trace.taskCompletedWithError(task, t);
				} finally {
					current.set(null);
				}
			}
		};
		
	}
}
