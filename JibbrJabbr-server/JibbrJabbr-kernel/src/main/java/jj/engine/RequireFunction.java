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
package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.CoreConfiguration;
import jj.configuration.Configuration;
import jj.resource.ResourceFinder;
import jj.script.CurrentScriptContext;
import jj.script.ModuleScriptBundle;
import jj.script.RequiredModule;
import jj.script.ScriptBundleFinder;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@Singleton
class RequireFunction extends BaseFunction implements HostObject, ContributesScript {

	private static final long serialVersionUID = -3809338081179905958L;
	
	private static final String REQUIRE = "//require";
	
	private final Configuration configuration;
	private final CurrentScriptContext context;
	private final ScriptBundleFinder scriptBundleFinder;
	private final ResourceFinder resourceFinder;
	
	@Inject
	RequireFunction(
		final Configuration configuration,
		final CurrentScriptContext context,
		final ScriptBundleFinder scriptBundleFinder,
		final ResourceFinder resourceFinder
	) {
		this.configuration = configuration;
		this.context = context;
		this.scriptBundleFinder = scriptBundleFinder;
		this.resourceFinder = resourceFinder;
	}

	@Override
	public String name() {
		return REQUIRE;
	}

	@Override
	public boolean constant() {
		return true;
	}

	@Override
	public boolean readonly() {
		return true;
	}

	@Override
	public boolean permanent() {
		return true;
	}

	@Override
	public boolean dontenum() {
		return true;
	}
	
	private String toModuleIdentifier(final String input) {
		String moduleIdentifier = null;
		if (input.startsWith(".")) {
			
			Object obj = ScriptableObject.getProperty(context.scriptBundle().scope(), "module");
			assert obj instanceof Scriptable : "'module' can't be found in the current execution scope, something wasn't built correctly";
			
			Scriptable module = (Scriptable)obj;
			String base = String.valueOf(ScriptableObject.getProperty(module, "id"));
			
			// well this is ridiculous but it works
			moduleIdentifier =
				configuration.get(CoreConfiguration.class).appPath().relativize(
					configuration.get(CoreConfiguration.class).appPath().resolve(base).getParent().resolve(input).normalize()
				).toString();
			
			// we allow modules outside the document root - after all, this is triggered internally
			// so presumably the author knows what they intend.
		} else {
			moduleIdentifier = input;
		}
		
		return moduleIdentifier;
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		
		String moduleIdentifier = toModuleIdentifier(String.valueOf(args[0]));
		
		ModuleScriptBundle scriptBundle = scriptBundleFinder.forBaseNameAndModuleIdentifier(context.baseName(), moduleIdentifier);
		
		// if we have an up-to-date script bundle, just return exports,
		// otherwise we need a continuation to start processing it properly,
		// mainly because if we don't do this as its own top call, then any
		// continuation inside the module will fail because there will be the
		// pending top call caused by this function.  continuations are like
		// violence, any problem can be solved by using MOAR!
		
		if (scriptBundle == null || 
			resourceFinder.findResource(scriptBundle.scriptResource()) != scriptBundle.scriptResource()) {
			throw context.prepareContinuation(new RequiredModule(moduleIdentifier, context));
		}
		
		return scriptBundle.exports();
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String script() {
		// needs to inspect the result and see if it's an exception, and if so throw it
		return "function require(id) {" +
					"if (!id || typeof id != 'string') throw new TypeError('argument to require must be a valid module identifier'); " +
					"var result = global['" + REQUIRE + "'](id); " +
					"if (result['getClass'] && java.jj.hostapi.RequiredModuleException.isAssignableFrom(result.getClass())) { " +
						"throw result;" + 
					"} " +
					"return result;" +
				"}";
	}

}
