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

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.ResourceInstanceCreator;
import jj.script.AbstractScriptEnvironmentCreator;
import jj.script.ScriptEnvironmentInitializer;

/**
 * not sure this is sticking around... API may just become a registry of InternalModuleScriptEnvironment things.  might
 * @author jason
 *
 */
@Singleton
public class APIBootstrapScriptEnvironmentCreator extends AbstractScriptEnvironmentCreator<APIBootstrapScriptEnvironment> {
	
	private final ResourceInstanceCreator creator;
	
	@Inject
	APIBootstrapScriptEnvironmentCreator(
		final ResourceInstanceCreator creator,
		final ScriptEnvironmentInitializer initializer
	) {
		super(initializer);
		this.creator = creator;
	}
	

	@Override
	public Class<APIBootstrapScriptEnvironment> type() {
		return APIBootstrapScriptEnvironment.class;
	}

	@Override
	public boolean canLoad(String name, Object... args) {
		
		return false;
	}

	@Override
	public APIBootstrapScriptEnvironment createScriptEnvironment(String name, Object... args) throws IOException {
		return creator.createResource(APIBootstrapScriptEnvironment.class, cacheKey(Virtual, name, args), Virtual, name, args);
	}

}
