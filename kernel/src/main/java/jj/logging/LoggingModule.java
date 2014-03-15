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

import jj.JJModule;

/**
 * @author jason
 *
 */
public class LoggingModule extends JJModule {
	
	private final boolean isTest;
	
	public LoggingModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		// this gets instantiated before anything might write to a log
		// actually that won't matter anymore soon! yay! the "test" parameter can get killed off
		bind(LogConfigurator.class).toInstance(new LogConfigurator(isTest));
		
		bind(EmergencyLog.class).to(SystemLogger.class);
		addStartupListenerBinding().to(SystemLogger.class);
		
		bindLoggedEvents().annotatedWith(EmergencyLogger.class).toLogger(EmergencyLogger.NAME);
		
	}

}
