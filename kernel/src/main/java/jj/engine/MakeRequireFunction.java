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

import jj.configuration.Configuration;
import jj.resource.ResourceFinder;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.RequiredModule;
import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author jason
 *
 */
@Singleton
class MakeRequireFunction extends BaseFunction implements HostObject, ContributesScript {

	private static final long serialVersionUID = -3809338081179905958L;
	
	private static final String REQUIRE = "//require";
	
	private final Configuration configuration;
	private final CurrentScriptEnvironment env;
	private final ResourceFinder resourceFinder;
	
	@Inject
	MakeRequireFunction(
		final Configuration configuration,
		final CurrentScriptEnvironment env,
		final ResourceFinder resourceFinder
	) {
		this.configuration = configuration;
		this.env = env;
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
	
	private String toModuleIdentifier(final String input, final String base) {
		// crazytimes! can probably be simplified
		return configuration.appPath().relativize(
			configuration.appPath().resolve(base).resolveSibling(input).normalize()
		).toString();
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		
		String moduleIdentifier = toModuleIdentifier(String.valueOf(args[0]), String.valueOf(args[1]));
		
		ScriptEnvironment parent = env.currentRootScriptEnvironment();
		
		RequiredModule requiredModule = new RequiredModule(parent, moduleIdentifier);
		
		ModuleScriptEnvironment scriptEnvironment =
			resourceFinder.findResource(
				ModuleScriptEnvironment.class,
				moduleIdentifier,
				requiredModule
			);
		
		
		// if we find a ModuleScriptEnvironment, return the exports, even
		// if it is still being initialized.  this will happen in circular
		// dependency scenarios.  MAKE IT CLEAR IN THE DOCS that this can
		// happen.  node does the same thing so it's fair
		
		// on the continuation side, the system does wait for initialization to
		// finish before restarting the waiting task, because if multiple requests
		// for the same non-existent module result in continuations, then it's
		// probably a lot of requests hitting the same uninitialized script at
		// once
		
		// it may be possible to determine the circular invocation and break it by
		// returning only in that case, thereby allowing most required modules
		// to be complete on parent resumption, but i'm not yet sure what that would
		// take
		
		// not sure what can guard against a cycle in this environment, though.
		// would involve that execution trace concept for sure
		
		// if there is no ModuleScriptEnvironment found, then we need a 
		// continuation to start processing it properly
		
		// this is because if we don't do this as its own top call, then any
		// continuation inside the module will fail because there will be the
		// pending top call caused by this function.  continuations are like
		// violence, any problem can be solved by using MOAR!
		
		if (scriptEnvironment == null) {
			throw env.preparedContinuation(requiredModule);
		}
		
		return scriptEnvironment.exports();
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String script() {
		// the require function as a user API is 
		// actually created individually in each 
		// scope when the scope is created by 
		// calling this function
		return "global['//makeRequire'] = function(module) {" +
					"return function(id) {" +
					"if (!id || typeof id != 'string') throw new TypeError('argument to require must be a valid module identifier'); " +
					"var result = global['" + REQUIRE + "'](id, module.id); " +
					"if (result['getCause']) { " +
						"throw result;" + 
					"} " +
					"return result;" +
				"}}";
	}

}
