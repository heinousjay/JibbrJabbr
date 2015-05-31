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
	
	static final String CONTEXT_SPEC = "spec";
	static final String CONTEXT_TARGET = "target";
	static final String CONTEXT_RUNNER = "runner";
	
	private final TaskRunner taskRunner;
	private final Publisher publisher;
	
	@Inject
	SpecCoordinator(
		final TaskRunner taskRunner,
		final Publisher publisher
	) {
		this.taskRunner = taskRunner;
		this.publisher = publisher;
	}
	
	@Listener
	void on(final ScriptEnvironmentInitialized event) {
		// right now, checking this way for testing purposes, to let mocks in
		if (JasmineScriptEnvironment.class.isAssignableFrom(event.scriptEnvironment().getClass())) {
			taskRunner.execute(new SpecEvaluationTask((JasmineScriptEnvironment)event.scriptEnvironment()));
		}
	}
	
	private final class SpecEvaluationTask extends ScriptTask<JasmineScriptEnvironment> {
		
		SpecEvaluationTask(final JasmineScriptEnvironment scriptEnvironment) {
			super("spec execution for " + scriptEnvironment, scriptEnvironment);
		}

		@Override
		protected void begin() throws Exception {
			pendingKey = scriptEnvironment.execute(scriptEnvironment.specScript());
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			publisher.publish(new JasmineTestError(scriptEnvironment, CONTEXT_SPEC, cause));
			return true;
		}
		
		@Override
		protected void complete() throws Exception {
			taskRunner.execute(new TargetEvaluationTask(scriptEnvironment));
		}
	}
	
	private final class TargetEvaluationTask extends ScriptTask<JasmineScriptEnvironment> {
		
		TargetEvaluationTask(final JasmineScriptEnvironment scriptEnvironment) {
			super("target execution for " + scriptEnvironment, scriptEnvironment);
		}

		@Override
		protected void begin() throws Exception {
			pendingKey = scriptEnvironment.execute(scriptEnvironment.targetScript());
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			publisher.publish(new JasmineTestError(scriptEnvironment, CONTEXT_TARGET, cause));
			return true;
		}
		
		@Override
		protected void complete() throws Exception {
			taskRunner.execute(new RunnerEvaluationTask(scriptEnvironment));
		}
	}
	
	private final class RunnerEvaluationTask extends ScriptTask<JasmineScriptEnvironment> {
		
		RunnerEvaluationTask(final JasmineScriptEnvironment scriptEnvironment) {
			super("runner execution for " + scriptEnvironment, scriptEnvironment);
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			publisher.publish(new JasmineTestError(scriptEnvironment, CONTEXT_RUNNER, cause));
			return true;
		}

		@Override
		protected void begin() throws Exception {
			pendingKey = scriptEnvironment.execute(scriptEnvironment.runnerScript());
		}
	}
}
