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

import io.netty.handler.codec.http.HttpMethod;
import jj.configuration.ConfigurationObjectBinder;
import jj.configuration.resolution.APIPaths;
import jj.configuration.resolution.AssetPaths;
import jj.conversion.Converter;
import jj.engine.HostObject;
import jj.execution.ExecutorBinder;
import jj.http.server.methods.HttpMethodHandlerBinder;
import jj.http.server.websocket.WebSocketConnectionHostBinder;
import jj.logging.LoggingBinder;
import jj.resource.ResourceBinder;
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
	
	private ResourceBinder resources;
	
	private ContinuationProcessorBinder continuationProcessors;
	
	private ExecutorBinder executors;
	
	private LoggingBinder loggers;
	
	private ConfigurationObjectBinder configurationObjects;
	
	private WebSocketConnectionHostBinder webSocketConnectionHosts;
	
	private HttpMethodHandlerBinder httpMethodHandlerBinder;
	
	private Multibinder<JJServerStartupListener> startupListeners;
	private Multibinder<Converter<?, ?>> converters;
	private Multibinder<HostObject> hostObjects;
	private Multibinder<String> assetPaths;
	private Multibinder<String> apiPaths;

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
	
	protected void addAPIModulePath(String path) {
		if (apiPaths == null) {
			apiPaths = Multibinder.newSetBinder(binder(), String.class, APIPaths.class);
		}

		apiPaths.addBinding().toInstance(path);
	}
	
	protected ResourceBinder bindCreation() {
		if (resources == null) {
			resources = new ResourceBinder(binder());
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
	
	protected ConfigurationObjectBinder bindConfiguration() {
		if (configurationObjects == null) {
			configurationObjects = new ConfigurationObjectBinder(binder());
		}
		return configurationObjects;
	}
	
	protected WebSocketConnectionHostBinder bindWebSocketConnection() {
		if (webSocketConnectionHosts == null) {
			webSocketConnectionHosts = new WebSocketConnectionHostBinder(binder());
		}
		return webSocketConnectionHosts;
	}
	
	protected HttpMethodHandlerBinder.With handle(HttpMethod method) {
		if (httpMethodHandlerBinder == null) {
			httpMethodHandlerBinder = new HttpMethodHandlerBinder(binder());
		}
		return httpMethodHandlerBinder.handle(method);
	}
}
