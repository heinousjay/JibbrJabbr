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

import jj.resource.*;
import jj.server.ServerLocation;

/**
 * @author jason
 *
 */
public abstract class AbstractScriptEnvironmentCreator<T extends AbstractScriptEnvironment<A>, A> extends SimpleResourceCreator<T, A> {
	
	protected static final Location Virtual = ServerLocation.Virtual;
	
	@Singleton
	public static class Dependencies extends SimpleResourceCreator.Dependencies {
		
		protected final ScriptEnvironmentInitializer initializer;

		@Inject
		protected Dependencies(
			final PathResolver pathResolver,
			final ResourceInstanceCreator creator,
			final ResourceIdentifierMaker resourceIdentifierMaker,
			final ScriptEnvironmentInitializer initializer
		) {
			super(pathResolver, creator, resourceIdentifierMaker);
			this.initializer = initializer;
		}
		
	}
	
	protected final ScriptEnvironmentInitializer initializer;
	
	protected AbstractScriptEnvironmentCreator(final Dependencies dependencies) {
		super(dependencies);
		this.initializer = dependencies.initializer; 
	}

	@Override
	public T create(Location base, String name, A argument) throws IOException {
		
		assert base == Virtual : "all ScriptEnvironments are Virtual";
		
		T result = createScriptEnvironment(name, argument);
		
		if (result != null) {
			initializer.initializeScript(result);
		}
		
		return result;
	}
	
	protected abstract T createScriptEnvironment(String name, A argument) throws IOException;
	
	@Override
	protected URI uri(Location base, String name, A argument) {
		assert base == Virtual : "all ScriptEnvironments are Virtual";
		return URI.create(name);
	}

}
