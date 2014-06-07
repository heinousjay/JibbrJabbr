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

import jj.JJModule;
import jj.configuration.resolution.Application;
import jj.configuration.resolution.PathResolver;

/**
 * @author jason
 *
 */
public class ConfigurationModule extends JJModule {

	@Override
	protected void configure() {
		
		addAssetPath("/jj/assets/");
		
		addAPIModulePath("/jj/configuration/api/");
		
		addStartupListenerBinding().to(ConfigurationScriptPreloader.class);
		
		bind(PathResolver.class).to(Application.class);
		
		bindCreation().of(ConfigurationScriptEnvironment.class).to(ConfigurationScriptEnvironmentCreator.class);
	}
}
