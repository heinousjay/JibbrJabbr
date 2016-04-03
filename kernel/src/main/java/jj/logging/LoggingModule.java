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
package jj.logging;

import static jj.logging.LoggingBinder.registerBuiltins;

import jj.JJModule;
import jj.configuration.BindsConfiguration;
import jj.server.BindsServerPath;

/**
 * @author jason
 *
 */
public class LoggingModule extends JJModule
	implements BindsConfiguration,
		BindsLogger,
	BindsServerPath {

	@Override
	protected void configure() {
		
		bindAPIModulePath("/jj/logging/api");
		
		bindConfiguration(LoggingConfiguration.class);
		
		bindStartupListener(LoggingConfigurator.class);
		
		bindStartupListener(SystemLogger.class);
		
		bindLoggedEventsAnnotatedWith(EmergencyLogger.class).toLogger(EmergencyLogger.NAME);
		
		registerBuiltins(binder());
	}

}
