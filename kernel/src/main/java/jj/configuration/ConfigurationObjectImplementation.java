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

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Fronts the {@link ConfigurationClassMaker} to cache the results
 * 
 * @author jason
 *
 */
@Singleton
class ConfigurationObjectImplementation {
	
	/** mapped interface to implementation */
	private final ConcurrentHashMap<Class<?>, Class<?>> implementations = new ConcurrentHashMap<>();
	
	private final ConfigurationClassMaker classLoader;
	
	@Inject
	ConfigurationObjectImplementation(final ConfigurationClassMaker classLoader) {
		this.classLoader = classLoader;
	}

	@SuppressWarnings("unchecked")
	<T> Class<? extends T> implementationClassFor(final Class<T> configurationInterface) {
		
		return (Class<? extends T>)implementations.computeIfAbsent(configurationInterface, 
			ci -> {
				try {
					return classLoader.make(ci);
				} catch (Exception e) {
					throw new AssertionError("couldn't implement " + ci, e);
				}
			}
		);
	}
	
}
