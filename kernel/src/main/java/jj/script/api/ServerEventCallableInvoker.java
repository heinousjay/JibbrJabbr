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
package jj.script.api;

import jj.execution.TaskRunner;
import jj.script.ScriptEnvironment;
import jj.script.ScriptTask;

import org.mozilla.javascript.Callable;

/**
 * base class for generated event invokers.  extending this directly might
 * make you feel good, but it won't have any particular effect within the system
 * unless you set it up correctly and inject an instance somewhere
 * @author jason
 *
 */
abstract class ServerEventCallableInvoker {
	
	private final TaskRunner taskRunner;

	private ScriptEnvironment<?> target;
	
	private Callable callable;
	
	private volatile boolean alive = true;
	
	protected ServerEventCallableInvoker(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
	}
	
	void invocationInstances(final ScriptEnvironment<?> target, final Callable callable) {
		this.target = target;
		this.callable = callable;
	}
	
	void kill() {
		alive = false;
	}
	
	protected void invoke(final Object event) {
		assert target != null : "invoker invoked without set target script environment";
		assert callable != null : "invoker invoked without set callable";
		
		if (alive) {
			String name = "invoking " + target.name() + " function with server event " + event.getClass().getName();
			taskRunner.execute(new ScriptTask<ScriptEnvironment<?>>(name, target) {
				@Override
				protected void begin() throws Exception {
					if (alive) {
						pendingKey = scriptEnvironment.execute(callable, event);
					}
				}
			});
		}
	}
}
