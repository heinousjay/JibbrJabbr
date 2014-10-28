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

import jj.InitializationException;
import jj.conversion.Converters;

/**
 * <p>
 * Parses the command line arguments and makes them available to the rest of the system
 * 
 * <p>
 * Arguments are expected to be "name=value" pairs, although I am considering providing
 * simple booleans just using presence.
 * 
 * @author jason
 *
 */
@Singleton
public class Arguments {
	
	private static final Pattern SPLITTER = Pattern.compile("(?<=[^\\s])=(?=[^\\s])");
	
	private final Map<String, String> arguments;
	private final Converters converters;
	
	private static <E extends Enum<E>> String from(Class<E> enumClass) {
		String name = enumClass.getSimpleName();
		StringBuilder out = new StringBuilder(name.length() * 2); // pretty worst case
		out.append(Character.toLowerCase(name.charAt(0)));
		for (int i = 1; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (Character.isUpperCase(c)) {
				out.append('-');
				c = Character.toLowerCase(c);
			}
			out.append(c);
		}
		return out.toString();
	}

	@Inject
	Arguments(final @CommandLine String[] arguments, final Converters converters) {
		this.arguments = readArguments(arguments);
		this.converters = converters;
	}
	
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
	
	public <E extends Enum<E>> E get(Class<E> enumClass) {
		return converters.convert(arguments.get(from(enumClass)), enumClass);
	}
	
	public <E extends Enum<E>> E get(Class<E> enumClass, E defaultValue) {
		E result = converters.convert(arguments.get(from(enumClass)), enumClass);
		
		return result == null ? defaultValue : result;
	}
	
	/**
	 * <p>
	 * Retrieve the indicated argument, converting to the desired type using {@link Converters}
	 * 
	 * @param name the name of the argument to retrieve
	 * @param type the desired type
	 * @return the argument value, if present and convertible. null otherwise
	 */
	public <T> T get(final String name, final Class<T> type) {
		return converters.convert(arguments.get(name), type);
	}
	
	/**
	 * <p>
	 * Retrieve the indicated argument, converting to the desired type using {@link Converters}, and
	 * returning the given default if necessary.
	 * 
	 * @param name the name of the argument to retrieve
	 * @param type the desired type
	 * @param defaultValue the desired default
	 * @return the argument value, if present and convertible. defaultValue otherwise
	 */
	public <T> T get(final String name, final Class<T> type, final T defaultValue) {
		T result = get(name, type);
		return arguments.containsKey(name) ? (result == null ? defaultValue : result) : defaultValue;
	}
	
	/**
	 * <p>
	 * Retrieve the raw value of the indicated argument, if present
	 * 
	 * @param name the name of the argument to retrieve
	 * @return the raw argument value, if present. null otherwise
	 */
	public String get(final String name) {
		return arguments.get(name);
	}
	
	@Override
	public String toString() {
		return arguments.toString();
	}
}
