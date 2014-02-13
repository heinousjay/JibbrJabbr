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
import jj.execution.ScriptTask;

/**
 * initializes a script environment, so that it is ready for execution.  if possible :D
 * 
 * @author jason
 *
 */
@Singleton
public class ScriptEnvironmentInitializer implements DependsOnScriptEnvironmentInitialization {
	
	private final TaskRunner taskRunner;
	
	private final ContinuationCoordinatorImpl continuationCoordinator;
	
	private final Publisher publisher;
	
	private static final class TaskOrKey {
		private final ContinuationPendingKey pendingKey;
		private final ScriptTask<? extends ScriptEnvironment> task;
		
		TaskOrKey(final ScriptTask<? extends ScriptEnvironment> task, final ContinuationPendingKey pendingKey) {
			this.task = task;
			this.pendingKey = pendingKey;
		}
	}

	private final ThreadLocal<HashMap<ScriptEnvironment, List<TaskOrKey>>> pendingInitialization = 
		new ThreadLocal<HashMap<ScriptEnvironment, List<TaskOrKey>>>() {
		
		@Override
		protected HashMap<ScriptEnvironment, List<TaskOrKey>> initialValue() {
			return new HashMap<>();
		}
	};
	
	/**
	 * @param name
	 * @param moduleScriptEnvironment
	 */
	@Inject
	ScriptEnvironmentInitializer(
		final TaskRunner taskRunner,
		final ContinuationCoordinatorImpl continuationCoordinator,
		final Publisher publisher
	) {
		this.taskRunner = taskRunner;
		this.continuationCoordinator = continuationCoordinator;
		this.publisher = publisher;
	}
	
	void initializeScript(AbstractScriptEnvironment se) {
		taskRunner.execute(new InitializerTask("initializing ScriptEnvironment at " + se.baseName(), se));
	}
	
	void scriptEnvironmentInitialized(ScriptEnvironment scriptEnvironment) {
		List<TaskOrKey> tasksOrKeys = pendingInitialization.get().remove(scriptEnvironment);
		if (tasksOrKeys != null) {
			for (TaskOrKey taskOrKey : tasksOrKeys) {
				if (taskOrKey.task != null) {
					taskRunner.execute(taskOrKey.task);
				} else if (taskOrKey.pendingKey != null) { 
					taskRunner.resume(taskOrKey.pendingKey, scriptEnvironment.exports());
				} else {
					throw new AssertionError("taskOrKey list was not maintained properly!");
				}
			}
		}
	}
	
	private List<TaskOrKey> getTaskOrKeyList(ScriptEnvironment scriptEnvironment) {
		List<TaskOrKey> taskOrKeys = pendingInitialization.get().get(scriptEnvironment);
		if (taskOrKeys == null) {
			pendingInitialization.get().put(scriptEnvironment, new ArrayList<TaskOrKey>());
			taskOrKeys = pendingInitialization.get().get(scriptEnvironment);
		}
		return taskOrKeys;
	}
	
	public void executeOnInitialization(ScriptEnvironment scriptEnvironment, ScriptTask<? extends ScriptEnvironment> task) {
		assert !scriptEnvironment.initialized() : "do not wait on scriptEnvironments that are initialized!";
		assert taskRunner.isScriptThreadFor(scriptEnvironment) : "only wait on script environments from their own thread!";
		getTaskOrKeyList(scriptEnvironment).add(new TaskOrKey(task, null));
	}
	
	/**
	 * register here to have a pendingKey resumed when a scriptEnvironment has transitioned to initialized
	 * @param scriptEnvironment
	 * @param pendingKey
	 */
	@Override
	public void resumeOnInitialization(final ScriptEnvironment scriptEnvironment, final ContinuationPendingKey pendingKey) {
		assert !scriptEnvironment.initialized() : "do not wait on scriptEnvironments that are initialized!";
		assert taskRunner.isScriptThreadFor(scriptEnvironment) : "only wait on script environments from their own thread!";
		getTaskOrKeyList(scriptEnvironment).add(new TaskOrKey(null, pendingKey));
	}
	
	// this should come from the script environment
	private enum State {
		Uninitialized,
		Initializing,
		Initialized,
		Broken
	}
	
	private class InitializerTask extends ScriptTask<AbstractScriptEnvironment> {
		
		private State state = State.Uninitialized;

		/**
		 * @param name
		 * @param scriptEnvironment
		 */
		protected InitializerTask(String name, AbstractScriptEnvironment scriptEnvironment) {
			super(name, scriptEnvironment);
		}

		@Override
		protected void run() throws Exception {
			
			try {
				switch (state) {
				case Uninitialized:
					startInitialization();
					break;
				case Initializing:
					continueInitialization();
					break;
				case Initialized:
				case Broken:
					throw new AssertionError("should not be called for initialized or broken script environments");
				}
			} catch (Throwable e) {
				broken();
				throw e;
			}
		}
		
		private void startInitialization() throws Exception {
			state = State.Initializing;
			scriptEnvironment.initializing(true);
			if (scriptEnvironment.script() != null) {
				try {
					pendingKey = continuationCoordinator.execute(scriptEnvironment);
				} catch (Exception e) {
					broken();
					throw e;
				}
			}
			
			if (pendingKey == null) {
				initialized();
				// and we're done
			}
		}
		
		private void continueInitialization() throws Exception {
			
			pendingKey = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, result);
			
			if (pendingKey == null) {
				initialized();
				// and we're done
			}
		}
		
		private void initialized() {
			state = State.Initialized;
			pendingKey = null;
			result = null;
			scriptEnvironment.initialized(true);

			publisher.publish(new ScriptEnvironmentInitialized(scriptEnvironment));
			checkParentResumption();
		}
		
		private void broken() {
			state = State.Broken;
			pendingKey = null;
			result = null;
			// mark the script environment somehow
			checkParentResumption();
		}
		
		private void checkParentResumption() {
			ContinuationPendingKey pendingKey = scriptEnvironment.pendingKey();
			if (pendingKey != null) {
				taskRunner.resume(pendingKey, scriptEnvironment.exports());
			}
			
			scriptEnvironmentInitialized(scriptEnvironment);
		}
	}

}
