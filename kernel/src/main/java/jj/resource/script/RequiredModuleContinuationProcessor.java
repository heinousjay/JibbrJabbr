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
package jj.resource.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.IOTask;
import jj.execution.JJExecutor;
import jj.resource.ResourceFinder;
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
// FIXME make this not public!
public class RequiredModuleContinuationProcessor implements ContinuationProcessor {
	
	private final JJExecutor executors;
	
	private final ResourceFinder finder;
	
	private final DependsOnScriptEnvironmentInitialization initializer;
	
	@Inject
	RequiredModuleContinuationProcessor(
		final JJExecutor executors,
		final ResourceFinder finder,
		final DependsOnScriptEnvironmentInitialization initializer
	) {
		this.executors = executors;
		this.finder = finder;
		this.initializer = initializer;
	}
	
	private void loadEnvironment(final RequiredModule requiredModule) {
		
		executors.execute(
			new IOTask("loading module [" + requiredModule.identifier() + "] from [" + requiredModule.parent().baseName() + "]") {
			
				@Override
				public void run() {
					
					// this will restart its parent automatically if it loads successfully,
					ModuleScriptEnvironment scriptEnvironment = 
						finder.loadResource(ModuleScriptEnvironment.class, requiredModule.identifier(), requiredModule);
					
					// so we only restart the parent when it's busted
					if (scriptEnvironment == null) {
						
						executors.resume(requiredModule.pendingKey(), new RequiredModuleException(requiredModule.identifier()));
					}
				}
			}
		);
	}

	@Override
	public void process(final ContinuationState continuationState) {
		
		final RequiredModule requiredModule = continuationState.continuableAs(RequiredModule.class);
		
		ModuleScriptEnvironment scriptEnvironment = 
			finder.findResource(
				ModuleScriptEnvironment.class,
				requiredModule.identifier(),
				requiredModule
			);
		
		// do we need to load it?
		if (scriptEnvironment == null) {
			loadEnvironment(requiredModule);
		}
		// if it's not yet initialized then we throw it in the smacker
		else if (!scriptEnvironment.initialized()) {
			initializer.resumeOnInitialization(scriptEnvironment, requiredModule.pendingKey());
		} 
		// otherwise ready to restart already.
		else {
			
			// if broken, restart with an error!
			executors.resume(requiredModule.pendingKey(), scriptEnvironment.exports());
		}
	}
}
