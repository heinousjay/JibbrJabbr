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

import static jj.script.ScriptExecutionState.*;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import jj.Closer;
import jj.resource.AbstractResource;
import jj.resource.ResourceCacheKey;

/**
 * @author jason
 *
 */
public abstract class AbstractScriptEnvironment extends AbstractResource implements ScriptEnvironment {
	
	protected final Provider<RhinoContext> contextProvider;
	
	protected final HashMap<ContinuationPendingKey, ContinuationPending> continuationPendings = new HashMap<>();
	
	// field injection! 
	// this is not my favored technique but the test has been written to
	// accommodate it, and it saves passing yet another dependency all throughout the system,
	// entirely through places that do not care about this.
	/**
	 * DO NOT OVERWRITE THE VALUE OF THIS FIELD!.  if is not final because it needs to be
	 * injected but it should be treated as if it were final
	 */
	@Inject
	private Provider<ContinuationPendingKey> pendingKeyProvider;
	
	ScriptExecutionState state = Unitialized;
	
	/**
	 * @param cacheKey
	 */
	protected AbstractScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final Provider<RhinoContext> contextProvider
	) {
		super(cacheKey);
		this.contextProvider = contextProvider;
	}
	
	@Override
	public ScriptableObject newObject() {
		try (RhinoContext context = contextProvider.get()) {
			return context.newObject(scope());
		}
	}
	
	// this is a nasty mess and really needs a wash

	@Override
	public boolean initialized() {
		return state == Initialized;
	}

	@Override
	public void initialized(boolean initialized) {
		if (initialized) {
			state = Initialized;
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
	
	ContinuationPendingKey createContinuationContext(final ContinuationPending continuationPending) {
		ContinuationPendingKey key = pendingKeyProvider.get();
		continuationPendings.put(key, continuationPending);
		captureContextForKey(key);
		return key;
	}
	
	ContinuationPending continuationPending(final ContinuationPendingKey key) {
		assert continuationPendings.containsKey(key) : "trying to retrieve a nonexistent continuation for " + key;
		return continuationPendings.remove(key);
	}
	
	protected void captureContextForKey(ContinuationPendingKey key) {
		// nothing to do in the abstract, but specific type will have things
		// DocumentScriptEnvironment needs to save connections and documents, for example
	}
	
	protected Closer restoreContextForKey(ContinuationPendingKey key) {
		return new Closer() {
			
			@Override
			public void close() {
				// nothing to do in the abstract
			}
		};
	}

	/**
	 * @return the exports property of the module object in the script's scope.  This could be
	 * anything at all, scripts are able to export whatever they want.
	 */
	public Object exports() {
		try (RhinoContext context = contextProvider.get()) {
			return scope() == null ? Undefined.instance : context.evaluateString(scope(), "module.exports", "evaluating exports");
		}
	}
	
	/**
	 * @return a pendingKey if the completion of the initialization task should resume something
	 * or null if nothing
	 * <p>
	 * maybe more things will be on this list later or the mechanism may expand
	 */
	protected ContinuationPendingKey pendingKey() {
		return null;
	}

	public String toString() {
		return new StringBuilder(getClass().getSimpleName())
			.append("[")
			.append(name())
			.append("@").append(sha1())
			.append("] {")
			.append("state=").append(state)
			.append("}")
			.toString();
	}

	protected ScriptableObject createLocalScope(final String moduleIdentifier, final ScriptableObject global) {
		try (RhinoContext context = contextProvider.get()) {
			
			// this technique allows for a "local" global scope that wraps a higher-level global
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
		// will be the name, which fortunately happens to be
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
