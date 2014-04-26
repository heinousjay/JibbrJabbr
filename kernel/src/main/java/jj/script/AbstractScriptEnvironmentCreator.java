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

import jj.configuration.AppLocation;
import jj.configuration.Location;
import jj.resource.AbstractResourceCreator;

/**
 * @author jason
 *
 */
public abstract class AbstractScriptEnvironmentCreator<T extends AbstractScriptEnvironment> extends AbstractResourceCreator<T> {
	
	protected static final AppLocation Virtual = AppLocation.Virtual;
	
	protected final ScriptEnvironmentInitializer initializer;
	
	protected AbstractScriptEnvironmentCreator(final ScriptEnvironmentInitializer initializer) {
		this.initializer = initializer; 
	}

	@Override
	public final T create(Location location, String name, Object... args) throws IOException {
		
		// TODO enable this!
		//assert location == AppLocation.Virtual : "all ScriptEnvironments are from the virtual location";
		
		T result = createScriptEnvironment(name, args);
		
		if (result != null) {
			initializer.initializeScript(result);
		}
		
		return result;
	}
	
	protected abstract T createScriptEnvironment(String name, Object... args) throws IOException;
	
	@Override
	protected URI uri(Location base, String name, Object... args) {
		// TODO enable this!
		//assert location == AppLocation.Virtual : "all ScriptEnvironments are from the virtual location";
		return URI.create(name);
	}

}
