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

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.google.inject.Binder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * <p>
 * Base module to register components into the JibbrJabbr system, supports
 * mix-in interfaces to update the configuration DSL
 * 
 * @author jason
 *
 */
public abstract class JJModule extends AbstractModule implements HasBinder {
	
	// this annotation is used to ensure that no one can inject Set<Object> and
	// get a weird variety of server components, because that's just not a
	// sensible thing to do
	@Qualifier
	@Target(PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface StartupListeners {}

	// nobody cares about this! but it's necessary
	// to make the startup work
	private Multibinder<Object> startupListeners;

	@Override
	public Binder binder() {
		return super.binder();
	}

	protected void bindStartupListener(Class<?> startupListenerClass) {
		assert startupListenerClass.isAnnotationPresent(Singleton.class) : "startup listeners must be singletons!";
		if (startupListeners == null) {
			startupListeners =  Multibinder.newSetBinder(binder(), Object.class, StartupListeners.class);
		}
		startupListeners.addBinding().to(startupListenerClass).asEagerSingleton();
	}
}
