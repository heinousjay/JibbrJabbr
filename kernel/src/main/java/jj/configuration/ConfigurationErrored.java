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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import jj.logging.EmergencyLogger;
import jj.logging.LoggedEvent;

/**
 * @author jason
 *
 */
@EmergencyLogger
public class ConfigurationErrored extends LoggedEvent {
	
	private final Map<String, List<String>> errors;
	
	ConfigurationErrored(final Map<String, List<String>> errors) {
		this.errors = errors;
	}

	@Override
	public void describeTo(Logger logger) {
		StringBuilder output = new StringBuilder();
		errors.forEach((name, messages) -> {
			output.append(name)
				.append(":\n");
			messages.forEach(message -> {
				output.append(" -").append(message).append("\n");
			});
		});
		
		logger.error("The configuration could not be loaded because of the following errors:\n{}", output);
	}

}
