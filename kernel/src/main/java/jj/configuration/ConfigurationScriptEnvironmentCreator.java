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

import jj.resource.ResourceInstanceCreator;
import jj.script.AbstractScriptEnvironmentCreator;
import jj.script.ScriptEnvironmentInitializer;

/**
 * 
 * 
 * @author jason
 *
 */
@Singleton
public class ConfigurationScriptEnvironmentCreator extends AbstractScriptEnvironmentCreator<ConfigurationScriptEnvironment> {
	
	static final String CONFIG_SCRIPT_NAME = "config.js";

	private final ResourceInstanceCreator creator;
	
	@Inject
	ConfigurationScriptEnvironmentCreator(
		final ScriptEnvironmentInitializer initializer,
		final ResourceInstanceCreator creator
	) {
		super(initializer);
		this.creator = creator;
	}

	@Override
	protected ConfigurationScriptEnvironment createScriptEnvironment(String name, Object... args) throws IOException {
		assert name == CONFIG_SCRIPT_NAME : "can only create " + CONFIG_SCRIPT_NAME;
		return creator.createResource(ConfigurationScriptEnvironment.class, resourceKey(Virtual, name, args), Virtual, name, args);
	}
	
	@Override
	protected URI uri(Location base, String name, Object... args) {
		assert name == CONFIG_SCRIPT_NAME : "can only load " + CONFIG_SCRIPT_NAME;
		return super.uri(base, name, args);
	}

}
