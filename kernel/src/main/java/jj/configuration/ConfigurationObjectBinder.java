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

import javax.inject.Inject;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * does a little funky dance so as to provide
 * injectable configuration objects
 * 
 * @author jason
 *
 */
public class ConfigurationObjectBinder {
	
	private final Binder binder;
	
	public ConfigurationObjectBinder(final Binder binder) {
		this.binder = binder;
	}

	public <T> void to(final Class<T> configurationInterface) {
		
		assert configurationInterface.isInterface() : "configuration objects must be interfaces";
		
		Provider<T> provider = new Provider<T>() {
			
			@Inject ConfigurationObjectImplementation configurationObjectImplementation;
			@Inject Injector injector;
			@Override
			public T get() {
				return injector.getInstance(configurationObjectImplementation.implementationClassFor(configurationInterface));
			}
		};
		
		binder.requestInjection(provider);
		
		binder.bind(configurationInterface).toProvider(provider);
	}
}
