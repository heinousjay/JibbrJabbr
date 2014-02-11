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

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.ResourceFinder;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.RequiredModule;
import jj.script.CurrentScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author jason
 *
 */
@Singleton
class RequireFunction extends BaseFunction {

	private static final long serialVersionUID = -3809338081179905958L;
	
	private final CurrentScriptEnvironment env;
	private final ResourceFinder resourceFinder;
	// we just need this to have a base upon which to determine location
	private final Path base = Paths.get("/requireBase");
	
	@Inject
	RequireFunction(
		final CurrentScriptEnvironment env,
		final ResourceFinder resourceFinder
	) {
		this.env = env;
		this.resourceFinder = resourceFinder;
	}
	
	private String toModuleIdentifier(final String identifier, final String parent) {
		
		String candidate = base.relativize(
			base.resolve(parent).resolveSibling(identifier).normalize()
		).toString();
		
		// if the resulting candidate starts with a . throw it out!
		if (candidate.startsWith(".")) throw new APIException(identifier + " is not valid");
		
		return candidate;
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		
		assert args != null && args.length == 2 : "two arguments required for the require API";
		
		String moduleIdentifier = toModuleIdentifier(String.valueOf(args[0]), String.valueOf(args[1]));
		
		RequiredModule requiredModule = new RequiredModule(env.currentRootScriptEnvironment(), moduleIdentifier);
		
		ModuleScriptEnvironment scriptEnvironment =
			resourceFinder.findResource(
				ModuleScriptEnvironment.class,
				moduleIdentifier,
				requiredModule
			);
		
		
		// if we have an up-to-date script execution environment, just return exports,
		// otherwise we need a continuation to start processing it properly,
		// mainly because if we don't do this as its own top call, then any
		// continuation inside the module will fail because there will be the
		// pending top call caused by this function.  continuations are like
		// violence, any problem can be solved by using MOAR!
		
		if (scriptEnvironment == null || !scriptEnvironment.initialized()) {
			throw env.preparedContinuation(requiredModule);
		}
		
		return scriptEnvironment.exports();
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException("cannot construct a require object");
	}

}
