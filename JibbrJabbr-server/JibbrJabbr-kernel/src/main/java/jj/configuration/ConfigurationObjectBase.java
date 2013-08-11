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

import jj.conversion.Converters;
import jj.resource.ResourceFinder;

/**
 * <p>
 * mediates between the generated configuration object
 * and the rest of the system.
 * </p>
 * <p>
 * has to be public so the internal classloader can see it,
 * but nothing should descend from this class statically
 * </p>
 * @author jason
 *
 */
public abstract class ConfigurationObjectBase {
	
	private final Arguments arguments;
	
	private final Converters converters;
	
	private final ResourceFinder resourceFinder;
	
	protected ConfigurationObjectBase(
		final Arguments arguments,
		final Converters converters,
		final ResourceFinder resourceFinder
	) {
		this.arguments = arguments;
		this.converters = converters;
		this.resourceFinder =resourceFinder;
	}
	
	
	protected <T> T readArgument(String name, String defaultValue, Class<T> resultClass) {
		String value = arguments.get(name);
		if (value == null) {
			value = defaultValue;
		}
		if (value != null) {
			return converters.convert(value, resultClass);
		}
		
		return null;
	}
}
