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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provides;

/**
 * @author jason
 *
 */
public class LoggingModule extends JJModule {
	
	public static final String ACCESS_LOGGER = "access";

	@Override
	protected void configure() {
		
	}
	
	@Provides @AccessLogger
	public Logger provideAccessLogger() {
		return LoggerFactory.getLogger(ACCESS_LOGGER);
	}

}
