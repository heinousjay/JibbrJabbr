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

import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
@Singleton
public class ModuleScriptEnvironmentCreator extends AbstractResourceCreator<ModuleScriptEnvironment> {
	
	/**
	 * 
	 */
	private static final String ARG_ERROR = "ModuleScriptEnvironmentCreator requires a ModuleParent argument";
	private final ResourceInstanceCreator creator;
	
	@Inject
	ModuleScriptEnvironmentCreator(final ResourceInstanceCreator creator) {
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
	public ModuleScriptEnvironment create(String baseName, Object... args) throws IOException {
		
		assert args.length == 1 && args[0] instanceof ModuleParent : ARG_ERROR;
		
		return creator.createResource(
			ModuleScriptEnvironment.class,
			cacheKey(baseName, args),
			baseName,
			Paths.get("/"),
			args
		);
	}
	
	@Override
	protected URI uri(String baseName, Object... args) {
		
		assert args.length == 1 && args[0] instanceof ModuleParent : ARG_ERROR;
		
		ModuleParent parent = (ModuleParent)args[0];
		
		return URI.create(parent + "#" + baseName);
	}

}
