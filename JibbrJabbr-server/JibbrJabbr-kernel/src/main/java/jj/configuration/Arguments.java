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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.CommandLine;
import jj.InitializationException;

/**
 * @author jason
 *
 */
@Singleton
class Arguments {
	
	private static final Pattern SPLITTER = Pattern.compile("(?<=[^\\s])=(?=[^\\s])");
	
	private final Map<String, String> arguments;

	@Inject
	Arguments(final @CommandLine String[] arguments) {
		this.arguments = readArguments(arguments);
	}

	/**
	 * @param arguments the arguments
	 * @return
	 */
	private Map<String, String> readArguments(String[] arguments) {
		HashMap<String, String> result = new HashMap<>();
		LinkedHashSet<String> badArgs = new LinkedHashSet<>();
		for (final String argument : arguments) {
			String[] particles = SPLITTER.split(argument);
			if (particles.length == 2) {
				result.put(particles[0], particles[1]);
			} else {
				badArgs.add(argument);
			}
		}
		
		if (!badArgs.isEmpty()) {
			throw new InitializationException("all arguments must be name=value pairs, the following were not understood\n" + badArgs);
		}
		return Collections.unmodifiableMap(result);
	}
	
	String get(final String name) {
		return arguments.get(name);
	}
	
	@Override
	public String toString() {
		return arguments.toString();
	}
}
