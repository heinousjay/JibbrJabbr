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
package jj.resource.script;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import javax.inject.Singleton;
import javax.inject.Inject;

import jj.resource.ResourceInstanceCreator;
import jj.script.AbstractScriptEnvironmentCreator;
import jj.script.ScriptEnvironmentInitializer;

/**
 * @author jason
 *
 */
@Singleton
public class ModuleScriptEnvironmentCreator extends AbstractScriptEnvironmentCreator<ModuleScriptEnvironment> {
	
	private static final String ARG_ERROR = "ModuleScriptEnvironmentCreator requires a RequiredModule argument";
	private final ResourceInstanceCreator creator;
	
	@Inject
	ModuleScriptEnvironmentCreator(final ScriptEnvironmentInitializer initializer, final ResourceInstanceCreator creator) {
		super(initializer);
		this.creator = creator;
	}

	@Override
	public Class<ModuleScriptEnvironment> type() {
		return ModuleScriptEnvironment.class;
	}

	@Override
	public boolean canLoad(String name, Object... args) {
		return false;
	}

	@Override
	protected ModuleScriptEnvironment createScriptEnvironment(String moduleIdentifier, Object... args) throws IOException {
		
		assert args.length == 1 && args[0] instanceof RequiredModule : ARG_ERROR;
		
		return creator.createResource(
			ModuleScriptEnvironment.class,
			cacheKey(moduleIdentifier, args),
			moduleIdentifier,
			Paths.get("/"),
			args
		);
	}
	
	@Override
	protected URI uri(String moduleIdentifier, Object... args) {
		
		assert args.length == 1 && args[0] instanceof RequiredModule : ARG_ERROR;
		
		RequiredModule requiredModule = (RequiredModule)args[0];
		
		return URI.create(requiredModule.parent().baseName() + "#" + moduleIdentifier);
	}

}
