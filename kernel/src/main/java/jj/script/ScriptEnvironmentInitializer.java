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

import jj.execution.JJExecutor;
import jj.execution.ResumableTask;
import jj.execution.ScriptTask;

/**
 * initializes a script environment, so that it is ready for execution.  if possible :D
 * 
 * @author jason
 *
 */
@Singleton
public class ScriptEnvironmentInitializer implements DependsOnScriptEnvironmentInitialization {
	
	private final JJExecutor executor;
	
	private final ContinuationCoordinator continuationCoordinator;

	private final ThreadLocal<HashMap<ScriptEnvironment, List<ContinuationPendingKey>>> pendingInitialization = 
		new ThreadLocal<HashMap<ScriptEnvironment, List<ContinuationPendingKey>>>() {
		
		@Override
		protected HashMap<ScriptEnvironment, List<ContinuationPendingKey>> initialValue() {
			return new HashMap<>();
		}
	};
	
	/**
	 * @param name
	 * @param moduleScriptEnvironment
	 */
	@Inject
	ScriptEnvironmentInitializer(
		final JJExecutor executor,
		final ContinuationCoordinator continuationCoordinator
	) {
		this.executor = executor;
		this.continuationCoordinator = continuationCoordinator;
	}
	
	void initializeScript(AbstractScriptEnvironment se) {
		executor.execute(new InitializerTask("initializing ScriptEnvironment at " + se.baseName(), se));
	}
	
	void scriptEnvironmentInitialized(ScriptEnvironment scriptEnvironment) {
		List<ContinuationPendingKey> keys = pendingInitialization.get().remove(scriptEnvironment);
		if (keys != null) {
			for (ContinuationPendingKey pendingKey : keys) {
				executor.resume(pendingKey, scriptEnvironment.exports());
			}
		}
	}
	
	/**
	 * register here to have a pendingKey resumed when a scriptEnvironment has transitioned to initialized
	 * @param scriptEnvironment
	 * @param pendingKey
	 */
	@Override
	public void resumeOnInitialization(final ScriptEnvironment scriptEnvironment, final ContinuationPendingKey pendingKey) {
		assert !scriptEnvironment.initialized() : "do not wait on scriptEnvironments that are initialized!";
		
		List<ContinuationPendingKey> keys = pendingInitialization.get().get(scriptEnvironment);
		if (keys == null) {
			pendingInitialization.get().put(scriptEnvironment, new ArrayList<ContinuationPendingKey>());
			keys = pendingInitialization.get().get(scriptEnvironment);
		}
		keys.add(pendingKey);
	}
	
	// this should come from the script environment
	private enum State {
		Uninitialized,
		Initializing,
		Initialized,
		Broken
	}
	
	private class InitializerTask extends ScriptTask<AbstractScriptEnvironment> implements ResumableTask {
		
		private ContinuationPendingKey pendingKey;
		private Object result;
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
			try {
				pendingKey = continuationCoordinator.execute(scriptEnvironment);
			} catch (Exception e) {
				broken();
				throw e;
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
				executor.resume(pendingKey, scriptEnvironment.exports());
			}
			
			scriptEnvironmentInitialized(scriptEnvironment);
		}

		@Override
		public ContinuationPendingKey pendingKey() {
			return pendingKey;
		}

		@Override
		public void resumeWith(Object result) {
			this.result = result;
		}
	
	}

}
