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

import jj.engine.EngineAPI;
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
	
	protected final EngineAPI api;
	
	protected ScriptExecutionState state = Unitialized;
	/**
	 * @param cacheKey
	 */
	protected AbstractScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final Publisher publisher,
		final RhinoContextMaker contextMaker,
		final EngineAPI api
	) {
		super(cacheKey);
		this.publisher = publisher;
		this.contextMaker = contextMaker;
		this.api = api;
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

	protected ScriptableObject createLocalScope(final String moduleIdentifier) {
		try (RhinoContext context = contextMaker.context()) {
			ScriptableObject local = context.newObject(api.global());
			local.setPrototype(api.global());
		    local.setParentScope(null);
		    
		    // setting up the 'module' property as described in 
		    // the commonjs module 1.1.1 specification
		    // in the case of the top-level server script, the id
		    // will be the baseName, which fortunately happens to be
		    // exactly what is required
		    ScriptableObject module = context.newObject(local);
		    module.defineProperty("id", moduleIdentifier, ScriptableObject.CONST);
		    local.defineProperty("module", module, ScriptableObject.CONST);
		    
		    return local;
		}
	}

}
