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
package jj.script.module;

import static jj.server.ServerLocation.Virtual;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceEvent;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoaded;
import jj.resource.ResourceLoader;
import jj.resource.ResourceNotFound;
import jj.script.ContinuationProcessor;
import jj.script.ContinuationState;
import jj.script.DependsOnScriptEnvironmentInitialization;

/**
 * Listens for RequiredModule continuations and dispatches them
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class RequiredModuleContinuationProcessor implements ContinuationProcessor {
	
	private final ResourceLoader resourceLoader;
	
	private final ResourceFinder resourceFinder;
	
	private final DependsOnScriptEnvironmentInitialization initializer;
	
	private final ConcurrentMap<RequiredModule, Boolean> waiters = new ConcurrentHashMap<>(4);
	
	@Inject
	RequiredModuleContinuationProcessor(
		ResourceLoader resourceLoader,
		ResourceFinder resourceFinder,
		DependsOnScriptEnvironmentInitialization initializer
	) {
		this.resourceLoader = resourceLoader;
		this.resourceFinder = resourceFinder;
		this.initializer = initializer;
	}
	
	private RequiredModule extractRequiredModule(ResourceEvent event) {
		RequiredModule result = null;
		if (event.argument instanceof RequiredModule) {
			result = (RequiredModule)event.argument;
		}
		return result;
	}
	
	@Listener
	void on(ResourceNotFound event) {
		RequiredModule requiredModule = extractRequiredModule(event);
		if (requiredModule != null && waiters.remove(requiredModule) != null) {
			requiredModule.pendingKey().resume(false);
		}
	}
	
	@Listener
	void on(ResourceLoaded event) {
		RequiredModule requiredModule = extractRequiredModule(event);
		if (requiredModule != null) {
			waiters.remove(requiredModule);
		}
	}
	
	private void loadEnvironment(RequiredModule requiredModule) {
		resourceLoader.loadResource(ModuleScriptEnvironment.class, Virtual, requiredModule.identifier(), requiredModule);
		Boolean result = waiters.putIfAbsent(requiredModule, Boolean.TRUE);
		assert (result == null) : "something is crossed up in the " + getClass();
	}

	@Override
	public void process(ContinuationState continuationState) {
		
		final RequiredModule requiredModule = continuationState.continuationAs(RequiredModule.class);
		
		ModuleScriptEnvironment scriptEnvironment = 
			resourceFinder.findResource(
				ModuleScriptEnvironment.class,
				Virtual,
				requiredModule.identifier(),
				requiredModule
			);
		
		// do we need to load it?
		if (scriptEnvironment == null) {
			loadEnvironment(requiredModule);
		}
		
		// if it's dead or broken then restart with an error
		else if (scriptEnvironment.initializationDidError() || !scriptEnvironment.alive()) {
			requiredModule.pendingKey().resume(false);
		}
		
		// if it's not yet initialized then we throw it in the smacker
		else if (!scriptEnvironment.initialized()) {
			initializer.resumeOnInitialization(scriptEnvironment, requiredModule.pendingKey());
		} 
		
		// otherwise ready to restart already.
		else {
			requiredModule.pendingKey().resume(scriptEnvironment.exports());
		}
	}
}
