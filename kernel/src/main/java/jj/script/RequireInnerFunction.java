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

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.resolution.AppLocation;
import jj.resource.ResourceFinder;
import jj.script.module.ModuleScriptEnvironment;
import jj.script.module.RequiredModule;
import jj.script.module.RootScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author jason
 *
 */
@Singleton
class RequireInnerFunction extends BaseFunction {

	private static final long serialVersionUID = -3809338081179905958L;
	
	private final CurrentScriptEnvironment env;
	private final ResourceFinder resourceFinder;
	
	// just used for calculating paths
	private final Path base = Paths.get("/requireBase");
	
	@Inject
	RequireInnerFunction(
		final CurrentScriptEnvironment env,
		final ResourceFinder resourceFinder
	) {
		this.env = env;
		this.resourceFinder = resourceFinder;
	}
	
	private String toModuleIdentifier(final String input, final String parent) {
		// crazytimes! can probably be simplified
		return base.relativize(
			base.resolve(parent).resolveSibling(input).normalize()
		).toString();
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		
		String moduleIdentifier = toModuleIdentifier(String.valueOf(args[0]), String.valueOf(args[1]));
		
		RootScriptEnvironment parent = env.currentRootScriptEnvironment();
		
		RequiredModule requiredModule = new RequiredModule(parent, moduleIdentifier);
		
		ModuleScriptEnvironment scriptEnvironment =
			resourceFinder.findResource(
				ModuleScriptEnvironment.class,
				AppLocation.Virtual,
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
		throw new AssertionError("do not attempt to construct this function");
	}

}
