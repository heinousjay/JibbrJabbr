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
package jj;

import jj.conversion.Converter;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

/**
 * @author jason
 *
 */
public abstract class JJModule extends AbstractModule {
	
	private Multibinder<JJServerStartupListener> startupListeners;
	private Multibinder<JJServerShutdownListener> shutdownListeners;
	private Multibinder<Converter<?, ?>> converters;

	protected LinkedBindingBuilder<JJServerStartupListener> addStartupListenerBinding() {
		if (startupListeners == null) {
			startupListeners =  Multibinder.newSetBinder(binder(), JJServerStartupListener.class);
		}
		return startupListeners.addBinding();
	}

	protected LinkedBindingBuilder<JJServerShutdownListener> addShutdownListenerBinding() {
		if (shutdownListeners == null) {
			shutdownListeners =  Multibinder.newSetBinder(binder(), JJServerShutdownListener.class);
		}
		return shutdownListeners.addBinding();
	}
	
	protected LinkedBindingBuilder<Converter<?, ?>> addConverterBinding() {
		if (converters == null) {
			converters = Multibinder.newSetBinder(binder(), new TypeLiteral<Converter<?, ?>>() {});
		}
		return converters.addBinding();
	}

}