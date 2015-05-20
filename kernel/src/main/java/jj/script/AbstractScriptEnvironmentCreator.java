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

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.application.AppLocation;
import jj.resource.PathResolver;
import jj.resource.ResourceInstanceCreator;
import jj.resource.SimpleResourceCreator;
import jj.resource.Location;

/**
 * @author jason
 *
 */
public abstract class AbstractScriptEnvironmentCreator<T extends AbstractScriptEnvironment> extends SimpleResourceCreator<T> {
	
	protected static final AppLocation Virtual = AppLocation.Virtual;
	
	@Singleton
	public static class Dependencies extends SimpleResourceCreator.Dependencies {
		
		protected final ScriptEnvironmentInitializer initializer;

		@Inject
		protected Dependencies(
			final PathResolver pathResolver,
			final ResourceInstanceCreator creator,
			final ScriptEnvironmentInitializer initializer
		) {
			super(pathResolver, creator);
			this.initializer = initializer;
		}
		
	}
	
	protected final ScriptEnvironmentInitializer initializer;
	
	protected AbstractScriptEnvironmentCreator(final Dependencies dependencies) {
		super(dependencies);
		this.initializer = dependencies.initializer; 
	}

	@Override
	public T create(Location base, String name, Object... args) throws IOException {
		
		assert base == Virtual : "all ScriptEnvironments are Virtual";
		
		T result = createScriptEnvironment(name, args);
		
		if (result != null) {
			initializer.initializeScript(result);
		}
		
		return result;
	}
	
	protected abstract T createScriptEnvironment(String name, Object... args) throws IOException;
	
	@Override
	protected URI uri(Location base, String name, Object... args) {
		assert base == Virtual : "all ScriptEnvironments are Virtual";
		return URI.create(name);
	}

}
