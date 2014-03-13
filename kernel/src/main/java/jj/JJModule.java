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

import jj.configuration.AssetPaths;
import jj.conversion.Converter;
import jj.engine.HostObject;
import jj.execution.ExecutorBinder;
import jj.logging.LoggingBinder;
import jj.resource.ResourceCreatorBinder;
import jj.script.ContinuationProcessorBinder;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

/**
 * @author jason
 *
 */
public abstract class JJModule extends AbstractModule {
	
	private ResourceCreatorBinder resources;
	
	private ContinuationProcessorBinder continuationProcessors;
	
	private ExecutorBinder executors;
	
	private LoggingBinder loggers;
	
	private Multibinder<JJServerStartupListener> startupListeners;
	private Multibinder<Converter<?, ?>> converters;
	private Multibinder<HostObject> hostObjects;
	private Multibinder<String> assetPaths;

	protected LinkedBindingBuilder<JJServerStartupListener> addStartupListenerBinding() {
		if (startupListeners == null) {
			startupListeners =  Multibinder.newSetBinder(binder(), JJServerStartupListener.class);
		}
		return startupListeners.addBinding();
	}
	
	protected LinkedBindingBuilder<Converter<?, ?>> addConverterBinding() {
		if (converters == null) {
			converters = Multibinder.newSetBinder(binder(), new TypeLiteral<Converter<?, ?>>() {});
		}
		return converters.addBinding();
	}
	
	protected LinkedBindingBuilder<HostObject> addHostObjectBinding() {
		if (hostObjects == null) {
			hostObjects = Multibinder.newSetBinder(binder(), HostObject.class);
		}
		return hostObjects.addBinding();
	}
	
	protected void addAssetPath(String path) {
		if (assetPaths == null) {
			assetPaths = Multibinder.newSetBinder(binder(), String.class, AssetPaths.class);
		}

		assetPaths.addBinding().toInstance(path);
	}
	
	protected ResourceCreatorBinder bindCreation() {
		if (resources == null) {
			resources = new ResourceCreatorBinder(binder());
		}
		return resources;
	}

	protected ContinuationProcessorBinder dispatch() {
		if (continuationProcessors == null) {
			continuationProcessors = new ContinuationProcessorBinder(binder());
		}
		return continuationProcessors;
	}
	
	protected void bindExecutor(Class<?> executor) {
		if (executors == null) {
			executors = new ExecutorBinder(binder());
		}
		executors.addExecutor(executor);
	}
	
	protected LoggingBinder bindLoggedEvents() {
		if (loggers == null) {
			loggers = new LoggingBinder(binder());
		}
		return loggers;
	}
}
