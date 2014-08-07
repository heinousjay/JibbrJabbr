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
import javax.inject.Singleton;

import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import jj.configuration.resolution.AppLocation;
import jj.event.Publisher;
import jj.resource.AbstractResource;
import jj.resource.AbstractResourceInitializationListener;
import jj.resource.ResourceConfiguration;
import jj.resource.ResourceFinder;
import jj.resource.ResourceKey;
import jj.resource.ResourceName;
import jj.util.Clock;
import jj.util.Closer;

/**
 * @author jason
 *
 */
public abstract class AbstractScriptEnvironment extends AbstractResource implements ScriptEnvironment {
	
	// bundles up the dependencies for this object, so that descendents don't need to
	// to be updated when this changes, cause it just might change more!
	// package-private access on the fields for testing
	@Singleton
	public static class Dependencies extends AbstractResource.Dependencies {
		
		final Provider<RhinoContext> contextProvider;
		final Provider<ContinuationPendingKey> pendingKeyProvider;
		final RequireInnerFunction requireInnerFunction;
		final InjectFunction injectFunction;
		final Timers timers;
		
		@Inject
		Dependencies(
			final Clock clock,
			final ResourceConfiguration resourceConfiguration,
			final AbstractResourceInitializationListener aril,
			final ResourceKey cacheKey,
			final @ResourceName String name,
			final Provider<RhinoContext> contextProvider,
			final Provider<ContinuationPendingKey> pendingKeyProvider,
			final RequireInnerFunction requireInnerFunction,
			final InjectFunction injectFunction,
			final Timers timers,
			final Publisher publisher,
			final ResourceFinder resourceFinder
		) {
			super(clock, resourceConfiguration, aril, cacheKey, AppLocation.Virtual, name, publisher, resourceFinder);
			this.contextProvider = contextProvider;
			this.pendingKeyProvider = pendingKeyProvider;
			this.requireInnerFunction = requireInnerFunction;
			this.injectFunction = injectFunction;
			this.timers = timers;
		}
	}
	
	/**
	 * convenience to get to a RhinoContext for script execution
	 */
	protected final Provider<RhinoContext> contextProvider;
	
	private final HashMap<ContinuationPendingKey, ContinuationPending> continuationPendings = new HashMap<>();
	
	private final Dependencies dependencies;
	
	private ScriptExecutionState state = Unitialized;
	
	private Throwable initializationError;
	
	/**
	 * @param cacheKey
	 */
	protected AbstractScriptEnvironment(
		Dependencies dependencies
	) {
		super(dependencies);
		this.contextProvider = dependencies.contextProvider;
		this.dependencies = dependencies;
	}
	
	//protected abstract void initializeScopes();
	
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
	
	@Override
	public void initializationError(Throwable cause) {
		state = Errored;
		this.initializationError = cause;
	}
	
	@Override
	public Throwable initializationError() {
		return initializationError;
	}
	
	@Override
	public boolean initializationDidError() {
		return state == Errored;
	}
	
	protected void died() {
		state = Dead;
		publisher.publish(new ScriptEnvironmentDied(this));
	}
	
	ContinuationPendingKey createContinuationContext(final ContinuationPending continuationPending) {
		ContinuationPendingKey key = dependencies.pendingKeyProvider.get();
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
		return super.toString() + " {state=" + state + "}";
	}
	
	protected ScriptableObject createChainedScope(final ScriptableObject parent) {
		try (RhinoContext context = contextProvider.get()) {
			return context.newChainedScope(parent);
		}
	}
	
	protected ScriptableObject configureTimers(final ScriptableObject localScope) {
		assert !localScope.isSealed() : "cannot configure timers on a sealed scope";
		localScope.defineProperty("setInterval", dependencies.timers.setInterval, ScriptableObject.EMPTY);
		localScope.defineProperty("setTimeout", dependencies.timers.setTimeout, ScriptableObject.EMPTY);
		localScope.defineProperty("clearInterval", dependencies.timers.clearInterval, ScriptableObject.EMPTY);
		localScope.defineProperty("clearTimeout", dependencies.timers.clearTimeout, ScriptableObject.EMPTY);
		return localScope;
	}
	
	protected ScriptableObject configureInjectFunction(final ScriptableObject localScope) {
		return configureInjectFunction(localScope, InjectFunction.NAME);
	}
	
	protected ScriptableObject configureInjectFunction(final ScriptableObject localScope, final String name) {
		assert !localScope.isSealed() : "cannot configure inject function on a sealed scope";
		localScope.defineProperty(name, dependencies.injectFunction, ScriptableObject.CONST);
		return localScope;
	}
	
	protected ScriptableObject configureModuleObjects(
		final String moduleIdentifier,
		final ScriptableObject localScope
	) {
		return configureModuleObjects(moduleIdentifier, localScope, "require");
	}

	/**
	 * Configures top-level module objects in a given scope, including a require function and assignable exports
	 * that are made available via {@link #exports()}
	 * @param moduleIdentifier must be fully qualified or further module resolution will fail
	 * @param localScope must not be sealed
	 * @return localScope, post configuration, for chaining
	 */
	protected ScriptableObject configureModuleObjects(
		final String moduleIdentifier,
		final ScriptableObject localScope,
		final String requireFunctionName
	) {
		assert !localScope.isSealed() : "cannot configure module objects on a sealed scope";
		
		try (RhinoContext context = contextProvider.get()) {
			// setting up the 'module' property as described in 
			// the commonjs module 1.1.1 specification
			// in the case of the top-level server script, the id
			// will be the name, which fortunately happens to be
			// exactly what is required
			ScriptableObject module = context.newObject(localScope);
			ScriptableObject exports = context.newObject(localScope);
			module.defineProperty("id", moduleIdentifier, ScriptableObject.CONST);
			module.defineProperty("exports", exports, ScriptableObject.EMPTY);
			module.defineProperty("requireInner", dependencies.requireInnerFunction, ScriptableObject.EMPTY);
			
			localScope.defineProperty("module", module, ScriptableObject.CONST);
			localScope.defineProperty("exports", exports, ScriptableObject.CONST);
			
			
			// define the require method and the exports object here as well.
			// follow the node.js concept of module.exports === exports, and
			// assigning to module.exports changes the exports object,
			// potentially to a function
			// the ability to load this as a ScriptResource is implicit, i believe
			ScriptableObject require = (ScriptableObject)context.evaluateString(
				localScope,  
				"(function(module) {\n" +
					"var idFormat = /^(?:\\.?\\/)?[a-zA-Z][\\/\\w-]*$/;\n" +
					"var requireInner = module.requireInner;\n" +
					"return function(id) {\n" +
						"if (!id || typeof id != 'string' || !idFormat.test(id)) {\n" + 
							"throw new Error(id + ' is not a valid module identifier');\n" +
						"}\n" +
						"var result = requireInner(id, module.id);\n" +
						"if (result === null || result === false) {\n" +
							"throw new Error('module \"' + id + '\" cannot be found');\n" +
						"}\n" +
						"return result;\n" +
					"}\n" +
				"})(module);",
				AbstractScriptEnvironment.class.getSimpleName() + " require function definition"
			);
			
			localScope.defineProperty(requireFunctionName, require, ScriptableObject.CONST);
			
			ScriptableObject.deleteProperty(module, "requireInner");
		}
		
		return localScope;
	}

}
