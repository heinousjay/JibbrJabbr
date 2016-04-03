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
package jj.repl;

import jj.JJModule;
import jj.configuration.BindsConfiguration;
import jj.resource.BindsResourceCreation;
import jj.server.BindsServerPath;

/**
 * @author jason
 *
 */
public class ReplModule extends JJModule
	implements BindsConfiguration,
		BindsResourceCreation,
	BindsServerPath {

	@Override
	protected void configure() {
		bindAssetPath("/jj/repl/assets");
		bindAPIModulePath("/jj/repl/api");
		bindConfiguration(ReplConfiguration.class);
		bindStartupListener(ReplServer.class);
		createResource(ReplScriptEnvironment.class).using(ReplScriptEnvironmentCreator.class);
	}

}
