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
package jj.api;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.logging.EmergencyLog;
import jj.script.RhinoContext;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;

/**
 * component that sources a prepared rhino context for script execution.
 * @author jason
 *
 */
@Singleton
class API {
	
	private static final String REQUIRE = "//require";

	static final String PROP_CONVERT_ARGS = "//convertArgs";
	
	private final RequireFunction requireFunction;
	
	private final EmergencyLog logger;
	
	private final ScriptableObject global;
	
	@Inject
	API(
		final Provider<RhinoContext> contextProvider,
		final RequireFunction requireFunction,
		final EmergencyLog logger,
		final Set<APIContributor> apiContributors
	) {
		this.requireFunction = requireFunction;
		this.logger = logger;
		
		try (RhinoContext context = contextProvider.get()) {
			global = initializeGlobalScope(context, apiContributors);
		}
	}
	
	public ScriptableObject global() {
		return global;
	}
	
	private ScriptableObject initializeGlobalScope(final RhinoContext context, final Set<APIContributor> apiContributors) {
		final ScriptableObject global = context.initStandardObjects(true);
		
		// make sure the regex object is available
		context.evaluateString(global , "RegExp; java;", "jj-internal ensuring objects");
		
		// this way we can always get to the global
		context.evaluateString(global, "var global=this;", "jj-internal creating global");
		
		// it's ugly in java like this but it's a tiny lil function we want globally
		// TODO move this out!
		context.evaluateString(
			global,
			"global['" + PROP_CONVERT_ARGS + "'] = function(args) {" +
				"return JSON.stringify(Array.map(args, function(arg) {return arg;}));" +
			"}",
			"jj-internal " + PROP_CONVERT_ARGS
		);
		
		// the actual require function
		global.defineProperty(REQUIRE, requireFunction, ScriptableObject.CONST);
		
		// this gets defined here because everything needs it to survive
		context.evaluateString(
			global,
			"global['//makeRequire'] = function(module) {" +
				"return function(id) {" +
					"if (!id || typeof id !== 'string') throw 'argument to require must be a valid module identifier'; " +
					"var result = global['" + REQUIRE + "'](id, module.id); " +
					"if (result['getCause']) { " +
						"throw result;" + 
					"} " +
					"return result;" +
				"}" +
			"}",
			"jj-internal //makeRequire"
		);
		
		try {
		
			for (final APIContributor hostObject : apiContributors) {
				hostObject.contribute(global, context);
			}
		
		} catch (RhinoException re) {
			logger.error("", re);
			throw re;
		}
		
		global.sealObject();
		
		return global;
	}

}
