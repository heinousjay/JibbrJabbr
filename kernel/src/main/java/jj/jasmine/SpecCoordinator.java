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
package jj.jasmine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.execution.TaskRunner;
import jj.script.ContinuationCoordinator;
import jj.script.ScriptEnvironmentInitialized;
import jj.script.ScriptTask;

/**
 * Responsible for the actual running of specs.  listens for
 * a JasmineScriptEnvironment to be initialized, and kicks off
 * the ScriptTasks that will make the whole thing happen
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class SpecCoordinator {

	private final ContinuationCoordinator continuationCoordinator;
	private final TaskRunner taskRunner;
	private final Publisher publisher;
	
	@Inject
	SpecCoordinator(
		final ContinuationCoordinator continuationCoordinator,
		final TaskRunner taskRunner,
		final Publisher publisher
	) {
		this.continuationCoordinator = continuationCoordinator;
		this.taskRunner = taskRunner;
		this.publisher = publisher;
	}
	
	@Listener
	void scriptInitialized(final ScriptEnvironmentInitialized event) {
		// right now, checking this way for testing purposes, to let mocks in
		if (JasmineScriptEnvironment.class.isAssignableFrom(event.scriptEnvironment().getClass())) {
			taskRunner.execute(new SpecEvaluationTask((JasmineScriptEnvironment)event.scriptEnvironment(), continuationCoordinator));
		}
	}
	
	
	
	private final class SpecEvaluationTask extends ScriptTask<JasmineScriptEnvironment> {
		
		SpecEvaluationTask(
			final JasmineScriptEnvironment scriptEnvironment,
			final ContinuationCoordinator continuationCoordinator
		) {
			super("spec execution for " + scriptEnvironment, scriptEnvironment, continuationCoordinator);
		}

		@Override
		protected void begin() throws Exception {
			pendingKey = continuationCoordinator.execute(scriptEnvironment, scriptEnvironment.specScript());
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			publisher.publish(new JasmineTestError(scriptEnvironment, cause));
			return true;
		}
		
		@Override
		protected void complete() throws Exception {
			taskRunner.execute(new TargetEvaluationTask(scriptEnvironment, continuationCoordinator));
		}
	}
	
	private final class TargetEvaluationTask extends ScriptTask<JasmineScriptEnvironment> {
		
		TargetEvaluationTask(
			final JasmineScriptEnvironment scriptEnvironment,
			final ContinuationCoordinator continuationCoordinator
		) {
			super("target execution for " + scriptEnvironment, scriptEnvironment, continuationCoordinator);
		}

		@Override
		protected void begin() throws Exception {
			pendingKey = continuationCoordinator.execute(scriptEnvironment, scriptEnvironment.targetScript());
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			publisher.publish(new JasmineTestError(scriptEnvironment, cause));
			return true;
		}
		
		@Override
		protected void complete() throws Exception {
			taskRunner.execute(new RunnerEvaluationTask(scriptEnvironment, continuationCoordinator));
		}
	}
	
	private final class RunnerEvaluationTask extends ScriptTask<JasmineScriptEnvironment> {
		
		RunnerEvaluationTask(
			final JasmineScriptEnvironment scriptEnvironment,
			final ContinuationCoordinator continuationCoordinator
		) {
			super("runner execution for " + scriptEnvironment, scriptEnvironment, continuationCoordinator);
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			publisher.publish(new JasmineTestError(scriptEnvironment, cause));
			return true;
		}

		@Override
		protected void begin() throws Exception {
			pendingKey = continuationCoordinator.execute(scriptEnvironment, scriptEnvironment.runnerScript());
		}
	}
}
