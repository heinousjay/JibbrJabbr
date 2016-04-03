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
package jj.configuration;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.Location;
import jj.script.AbstractScriptEnvironmentCreator;

/**
 * 
 * 
 * @author jason
 *
 */
@Singleton
class ConfigurationScriptEnvironmentCreator extends AbstractScriptEnvironmentCreator<ConfigurationScriptEnvironment, Void> {
	
	static final String CONFIG_NAME = "config";
	static final String CONFIG_SCRIPT_NAME = CONFIG_NAME + ".js";
	
	@Inject
	ConfigurationScriptEnvironmentCreator(Dependencies dependencies) {
		super(dependencies);
	}

	@Override
	protected ConfigurationScriptEnvironment createScriptEnvironment(String name, Void argument) throws IOException {
		assert CONFIG_NAME.equals(name) : "can only create " + CONFIG_NAME;
		return creator.createResource(resourceIdentifierMaker.make(type(), Virtual, name, null));
	}
	
	@Override
	protected URI uri(Location base, String name, Void argument) {
		assert CONFIG_NAME.equals(name) : "can only load " + CONFIG_NAME;
		return super.uri(base, name, argument);
	}

}
