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

import static jj.script.ScriptExecutionState.Initialized;
import static jj.script.ScriptExecutionState.Initializing;
import static jj.script.ScriptExecutionState.Unitialized;

import org.mozilla.javascript.ScriptableObject;

import jj.event.Publisher;
import jj.resource.document.ExecutionEnvironmentInitialized;

/**
 * @author jason
 *
 */
public abstract class AbstractScriptExecutionEnvironment implements ScriptExecutionEnvironment {

	protected final Publisher publisher;
	
	protected final RhinoContextMaker contextMaker;
	
	protected ScriptExecutionState state = Unitialized;
	
	protected AbstractScriptExecutionEnvironment(final Publisher publisher, final RhinoContextMaker contextMaker) {
		this.publisher = publisher;
		this.contextMaker = contextMaker;
	}
	
	@Override
	public ScriptableObject newObject() {
		try (RhinoContext context = contextMaker.context()) {
			return context.newObject(scope());
		}
	}

	@Override
	public boolean initialized() {
		return state == Initialized;
	}

	@Override
	public void initialized(boolean initialized) {
		if (initialized) {
			state = Initialized;
			publisher.publish(new ExecutionEnvironmentInitialized(this));
		}
	}

	@Override
	public boolean initializing() {
		return state == Initializing;
	}

	@Override
	public void initializing(boolean initializing) {
		if (initializing && state == Unitialized) {
			state = Initializing;
		}
	}

	public String toString() {
		return new StringBuilder(getClass().getName())
			.append("[")
			.append(baseName()).append("/").append(scriptName())
			.append("@").append(sha1())
			.append("] {")
			.append("state=").append(state)
			.append("}")
			.toString();
	}

}
