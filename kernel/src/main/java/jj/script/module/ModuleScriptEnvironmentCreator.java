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
package jj.script.module;

import java.io.IOException;
import java.net.URI;

import javax.inject.Singleton;
import javax.inject.Inject;

import jj.resource.Location;
import jj.script.AbstractScriptEnvironmentCreator;

/**
 * @author jason
 *
 */
@Singleton
class ModuleScriptEnvironmentCreator extends AbstractScriptEnvironmentCreator<ModuleScriptEnvironment> {
	
	private static final String ARG_ERROR = "ModuleScriptEnvironmentCreator requires a RequiredModule argument";
	
	@Inject
	ModuleScriptEnvironmentCreator(final Dependencies dependencies) {
		super(dependencies);
	}
	
	private RequiredModule requiredModule(Object[] args) {
		assert args.length == 1 && args[0] instanceof RequiredModule : ARG_ERROR;
		return (RequiredModule)args[0];
	}

	@Override
	protected ModuleScriptEnvironment createScriptEnvironment(String moduleIdentifier, Object... args) throws IOException {
		return creator.createResource(
			ModuleScriptEnvironment.class,
			resourceKey(Virtual, moduleIdentifier, args),
			Virtual,
			moduleIdentifier,
			requiredModule(args)
		);
	}
	
	@Override
	protected URI uri(Location base, String moduleIdentifier, Object... args) {
		return requiredModule(args).uri();
	}

}
