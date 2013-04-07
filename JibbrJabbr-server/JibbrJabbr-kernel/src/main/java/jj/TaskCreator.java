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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class TaskCreator {
	
	private final Logger log = LoggerFactory.getLogger(TaskCreator.class);

	private final ExecutionTraceImpl trace;
	
	@Inject
	TaskCreator(
		final ExecutionTraceImpl trace
	) {
		this.trace = trace;
	}
	
	public Runnable prepareTask(final JJRunnable task) {
		
		final ExecutionTraceImpl.State state = task.ignoreInExecutionTrace() ? null : trace.save();
		
		return new Runnable() {
			
			@Override
			public final void run() {
				try {
					if (!task.ignoreInExecutionTrace()) {
						trace.restore(state);
						trace.addEvent(task.name());
					}
					task.run();
				} catch (OutOfMemoryError rethrow) {
					throw rethrow;
				} catch (Throwable t) {
					log.error("Problem running a task {}", task.name());
					log.error("", t);
				}
			}
		};
		
	}
}
