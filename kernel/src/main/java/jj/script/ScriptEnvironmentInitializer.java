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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.execution.TaskRunner;

/**
 * <p>
 * initializes a script environment, so that it is ready for execution.  if possible :D
 * 
 * @author jason
 *
 */
@Singleton
public class ScriptEnvironmentInitializer implements DependsOnScriptEnvironmentInitialization {
	
	private final TaskRunner taskRunner;
	
	private final IsThread isScriptThread;
	
	private final Publisher publisher;
	
	private static final class TaskOrKey {
		private final PendingKey pendingKey;
		private final ScriptTask<? extends ScriptEnvironment<?>> task;
		
		TaskOrKey(final ScriptTask<? extends ScriptEnvironment<?>> task, final PendingKey pendingKey) {
			this.task = task;
			this.pendingKey = pendingKey;
		}
	}

	private final ThreadLocal<HashMap<ScriptEnvironment<?>, List<TaskOrKey>>> pendingInitialization = 
		new ThreadLocal<HashMap<ScriptEnvironment<?>, List<TaskOrKey>>>() {
		
		@Override
		protected HashMap<ScriptEnvironment<?>, List<TaskOrKey>> initialValue() {
			return new HashMap<>();
		}
	};

	@Inject
	ScriptEnvironmentInitializer(
		final TaskRunner taskRunner,
		final IsThread isScriptThread,
		final Publisher publisher
	) {
		this.taskRunner = taskRunner;
		this.isScriptThread = isScriptThread;
		this.publisher = publisher;
	}
	
	void initializeScript(AbstractScriptEnvironment<?> se) {
		taskRunner.execute(new InitializerTask("initializing " + se, se));
	}
	
	void scriptEnvironmentInitialized(ScriptEnvironment<?> scriptEnvironment) {
		List<TaskOrKey> tasksOrKeys = pendingInitialization.get().remove(scriptEnvironment);
		if (tasksOrKeys != null) {
			for (TaskOrKey taskOrKey : tasksOrKeys) {
				if (taskOrKey.task != null) {
					taskRunner.execute(taskOrKey.task);
				} else if (taskOrKey.pendingKey != null) { 
					taskOrKey.pendingKey.resume(scriptEnvironment.exports());
				} else {
					throw new AssertionError("taskOrKey list was not maintained properly!");
				}
			}
		}
	}
	
	private List<TaskOrKey> getTaskOrKeyList(ScriptEnvironment<?> scriptEnvironment) {
		List<TaskOrKey> taskOrKeys = pendingInitialization.get().get(scriptEnvironment);
		if (taskOrKeys == null) {
			pendingInitialization.get().put(scriptEnvironment, new ArrayList<>());
			taskOrKeys = pendingInitialization.get().get(scriptEnvironment);
		}
		return taskOrKeys;
	}
	
	@Override
	public void executeOnInitialization(ScriptEnvironment<?> scriptEnvironment, ScriptTask<? extends ScriptEnvironment<?>> task) {
		assert isScriptThread.forScriptEnvironment(scriptEnvironment) : "only wait on script environments from their own thread!";
		assert !scriptEnvironment.initialized() : "do not wait on scriptEnvironments that are initialized!";
		getTaskOrKeyList(scriptEnvironment).add(new TaskOrKey(task, null));
	}
	
	/**
	 * register here to have a pendingKey resumed when a scriptEnvironment has transitioned to initialized
	 */
	@Override
	public void resumeOnInitialization(final ScriptEnvironment<?> scriptEnvironment, final PendingKey pendingKey) {
		assert isScriptThread.forScriptEnvironment(scriptEnvironment) : "only wait on script environments from their own thread!";
		assert !scriptEnvironment.initialized() : "do not wait on scriptEnvironments that are initialized!";
		getTaskOrKeyList(scriptEnvironment).add(new TaskOrKey(null, pendingKey));
	}
	
	private class InitializerTask extends ScriptTask<AbstractScriptEnvironment<?>> {

		protected InitializerTask(String name, AbstractScriptEnvironment<?> scriptEnvironment) {
			super(name, scriptEnvironment);
		}
		
		protected void begin() throws Exception {
			pendingKey = scriptEnvironment.beginInitializing();
		}
		
		protected void complete() throws Exception {
			if (pendingKey == null) {
				initialized();
			}
		}
		
		private void initialized() {
			scriptEnvironment.initialized(true);

			// and tell the world about it!
			publisher.publish(new ScriptEnvironmentInitialized(scriptEnvironment));
			scriptEnvironmentInitialized(scriptEnvironment);
			checkParentResumption(scriptEnvironment.exports());
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			scriptEnvironment.initializationError(cause);
			publisher.publish(new ScriptEnvironmentInitializationError(scriptEnvironment, cause));
			scriptEnvironmentInitialized(scriptEnvironment);
			checkParentResumption(false);
			return true;
		}
		
		private void checkParentResumption(Object result) {
			PendingKey pendingKey = scriptEnvironment.initializationContinuationPendingKey();
			if (pendingKey != null) {
				pendingKey.resume(result);
			}
		}
	}

}
