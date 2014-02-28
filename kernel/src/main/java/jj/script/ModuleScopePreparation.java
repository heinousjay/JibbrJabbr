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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.ScriptableObject;


@Singleton
class ModuleScopePreparation {

	private final MakeRequireFunction makeRequireFunction;
	
	@Inject
	ModuleScopePreparation(final MakeRequireFunction makeRequireFunction) {
		this.makeRequireFunction = makeRequireFunction;
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
