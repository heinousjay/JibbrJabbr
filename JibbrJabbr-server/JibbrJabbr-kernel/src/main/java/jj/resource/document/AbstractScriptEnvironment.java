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
package jj.resource.document;

import static jj.resource.document.ScriptExecutionState.*;

import org.mozilla.javascript.ScriptableObject;

import jj.event.Publisher;
import jj.resource.AbstractResourceBase;
import jj.resource.ResourceCacheKey;
import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;

/**
 * @author jason
 *
 */
public abstract class AbstractScriptEnvironment extends AbstractResourceBase implements ScriptEnvironment {


	protected final Publisher publisher;
	
	protected final RhinoContextMaker contextMaker;
	
	protected ScriptExecutionState state = Unitialized;
	
	/**
	 * @param cacheKey
	 */
	protected AbstractScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final Publisher publisher,
		final RhinoContextMaker contextMaker
	) {
		super(cacheKey);
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

	/**
	 * @return
	 */
	public Object exports() {
		try (RhinoContext context = contextMaker.context()) {
			return context.evaluateString(scope(), "module.exports", "returning exports"); 
		}
	}

	public String toString() {
		return new StringBuilder(getClass().getSimpleName())
			.append("[")
			.append(baseName())
			.append("@").append(sha1())
			.append("] {")
			.append("state=").append(state)
			.append("}")
			.toString();
	}

	protected ScriptableObject createLocalScope(final String moduleIdentifier, final ScriptableObject global) {
		try (RhinoContext context = contextMaker.context()) {
			
			// this technique allows for a "local" global scope that wraps a read-only global
			// scope that provides a server-wide API installation - effectively sealing the
			// API to prevent it from being manipulated and allowing it to be shared in a safe
			// manner
			ScriptableObject local = context.newObject(global);
			local.setPrototype(global);
			local.setParentScope(null);

			configureModuleObjects(moduleIdentifier, context, local);
			
			return local;
		}
	}

	protected void configureModuleObjects(final String moduleIdentifier, RhinoContext context, ScriptableObject local) {
		// setting up the 'module' property as described in 
		// the commonjs module 1.1.1 specification
		// in the case of the top-level server script, the id
		// will be the baseName, which fortunately happens to be
		// exactly what is required
		ScriptableObject module = context.newObject(local);
		ScriptableObject exports = context.newObject(local);
		module.defineProperty("id", moduleIdentifier, ScriptableObject.CONST);
		module.defineProperty("exports", exports, ScriptableObject.EMPTY);
		
		local.defineProperty("module", module, ScriptableObject.CONST);
		local.defineProperty("exports", exports, ScriptableObject.CONST);
		
		// define the require method and the exports object here as well.
		// follow the node.js concept of module.exports === exports, and
		// assigning to module.exports changes the exports object,
		// potentially to a function
		Object require = context.evaluateString(local, "global['//makeRequire'](module);", "making require");
		local.defineProperty(
			"require",
			require,
			ScriptableObject.CONST
		);
	}

}
