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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import jj.configuration.ConfigurationObjectBinder;
import jj.conversion.Converter;
import jj.engine.HostObject;
import jj.http.server.ServableResource;
import jj.http.server.ServableResourceBindingProcessor;
import jj.http.server.websocket.WebSocketConnectionHost;
import jj.http.server.websocket.WebSocketConnectionHostBindingProcessor;
import jj.logging.LoggingBinder;
import jj.logging.LoggingBinder.BindingBuilder;
import jj.resource.AbstractResource;
import jj.resource.ResourceBinder;
import jj.resource.SimpleResourceCreator;
import jj.script.Continuation;
import jj.script.ContinuationProcessor;
import jj.script.ContinuationProcessorBinder;
import jj.server.APIModulePaths;
import jj.server.APISpecPaths;
import jj.server.AssetPaths;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

/**
 * <p>
 * Base module to register components into the JibbrJabbr system
 * 
 * @author jason
 *
 */
public abstract class JJModule extends AbstractModule {
	
	// this annotation is used to ensure that no one can inject Set<Object> and
	// get a weird variety of server components, because that's just not a
	// sensible thing to do
	@Qualifier
	@Target(PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface StartupListeners {}

	private ResourceBinder resources;

	private ContinuationProcessorBinder continuationProcessors;

	private LoggingBinder loggers;

	private ConfigurationObjectBinder configurationObjects;

	// nobody cares about this! but it's necessary
	// to make the startup work
	private Multibinder<Object> startupListeners;
	private Multibinder<Converter<?, ?>> converters;
	private Multibinder<HostObject> hostObjects;
	private Multibinder<String> assetPaths;
	private Multibinder<String> apiModulePaths;
	private Multibinder<String> apiSpecPaths;

	protected void bindStartupListener(Class<?> startupListenerClass) {
		assert startupListenerClass.isAnnotationPresent(Singleton.class) : "startup listeners must be singletons!";
		if (startupListeners == null) {
			startupListeners =  Multibinder.newSetBinder(binder(), Object.class, StartupListeners.class);
		}
		startupListeners.addBinding().to(startupListenerClass).asEagerSingleton();
	}

	protected void bindConverter(Class<? extends Converter<?, ?>> converterClass) {
		if (converters == null) {
			converters = Multibinder.newSetBinder(binder(), new TypeLiteral<Converter<?, ?>>() {});
		}
		converters.addBinding().to(converterClass);
	}

	protected void bindHostObject(Class<? extends HostObject> hostObjectClass) {
		if (hostObjects == null) {
			hostObjects = Multibinder.newSetBinder(binder(), HostObject.class);
		}
		hostObjects.addBinding().to(hostObjectClass);
	}

	protected void bindAssetPath(String path) {
		assert path != null && path.startsWith("/") : "path must be present and start with /";
		if (assetPaths == null) {
			assetPaths = Multibinder.newSetBinder(binder(), String.class, AssetPaths.class);
		}
		assetPaths.addBinding().toInstance(path);
	}
	
	protected void bindAPIModulePath(String path) {
		assert path != null && path.startsWith("/") : "path must be present and start with /";
		if (apiModulePaths == null) {
			apiModulePaths = Multibinder.newSetBinder(binder(), String.class, APIModulePaths.class);
		}
		apiModulePaths.addBinding().toInstance(path);
	}
	
	protected void bindAPISpecPath(String path) {
		assert path != null && path.startsWith("/") : "path must be present and start with /";
		if (apiSpecPaths == null) {
			apiSpecPaths = Multibinder.newSetBinder(binder(), String.class, APISpecPaths.class);
		}
		apiSpecPaths.addBinding().toInstance(path);
	}

	protected <T extends AbstractResource<A>, A> LinkedBindingBuilder<SimpleResourceCreator<T, A>> bindCreationOf(Class<T> resourceClass) {
		if (resources == null) {
			resources = new ResourceBinder(binder())
				.addResourceBindingProcessor(ServableResource.class, new ServableResourceBindingProcessor(binder()))
				.addResourceBindingProcessor(WebSocketConnectionHost.class, new WebSocketConnectionHostBindingProcessor(binder()));
		}
		return resources.of(resourceClass);
	}

	protected LinkedBindingBuilder<ContinuationProcessor> bindContinuationProcessingOf(Class<? extends Continuation> continuationClass) {
		if (continuationProcessors == null) {
			continuationProcessors = new ContinuationProcessorBinder(binder());
		}
		return continuationProcessors.continuationOf(continuationClass);
	}

	protected void bindExecutor(Class<?> executor) {
		assert executor.isAnnotationPresent(Singleton.class);
		binder().bind(executor).asEagerSingleton();
	}

	protected BindingBuilder bindLoggedEventsAnnotatedWith(Class<? extends Annotation> annotation) {
		if (loggers == null) {
			loggers = new LoggingBinder(binder());
		}
		return loggers.annotatedWith(annotation);
	}

	protected void bindConfiguration(Class<?> configurationInterface) {
		if (configurationObjects == null) {
			configurationObjects = new ConfigurationObjectBinder(binder());
		}
		configurationObjects.to(configurationInterface);
	}
}
