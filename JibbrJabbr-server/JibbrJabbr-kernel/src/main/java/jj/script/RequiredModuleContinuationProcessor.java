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
package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.RequiredModuleException;
import jj.execution.IOTask;
import jj.execution.JJExecutor;
import jj.resource.ResourceFinder;
import jj.resource.document.ModuleParent;
import jj.resource.document.ModuleScriptEnvironment;

/**
 * @author jason
 *
 */
@Singleton
class RequiredModuleContinuationProcessor implements ContinuationProcessor {
	
	private final CurrentScriptContext context;
	
	private final JJExecutor executors;
	
	private final ScriptRunnerInternal scriptRunner;
	
	private final ResourceFinder finder;
	
	@Inject
	RequiredModuleContinuationProcessor(
		final CurrentScriptContext context,
		final JJExecutor executors,
		final ScriptRunnerInternal scriptRunner,
		final ResourceFinder finder
	) {
		this.context = context;
		this.executors = executors;
		this.scriptRunner = scriptRunner;
		this.finder = finder;
	}
	
	private void loadEnvironment(final RequiredModule requiredModule, final ModuleParent moduleParent) {
		
		executors.execute(
			new IOTask("loading module [" + requiredModule.identifier() + "] from [" + context.baseName() + "]") {
			
				@Override
				public void run() {
					ModuleScriptEnvironment scriptEnvironment = 
						finder.loadResource(ModuleScriptEnvironment.class, requiredModule.identifier(), moduleParent);
					
					if (scriptEnvironment != null) {
						
						if (scriptEnvironment.initialized() || scriptEnvironment.initializing()) {
							restartInProgress(requiredModule, scriptEnvironment);
						} else {
							scriptRunner.submit(requiredModule, scriptEnvironment);
						}
						
					} else {
						
						scriptRunner.submit(
							"required module " + requiredModule.identifier() + " error result in [" + context.baseName() + "]",
							requiredModule.parentContext(),
							requiredModule.pendingKey(),
							new RequiredModuleException(requiredModule.identifier())
						);
					}
				}
			}
		);
	}

	@Override
	public void process(final ContinuationState continuationState) {
		final RequiredModule requiredModule = continuationState.requiredModule();
		final ModuleParent moduleParent = new ModuleParent(context.documentScriptEnvironment());
		
		
		ModuleScriptEnvironment scriptEnvironment = 
			finder.findResource(
				ModuleScriptEnvironment.class,
				requiredModule.identifier(),
				moduleParent
			);
		
		// decision 1: do we need i/o?
		if (scriptEnvironment == null) {
			loadEnvironment(requiredModule, moduleParent);
		}	
		// decision 2: do we need to initialize the execution environment?
		else if (!scriptEnvironment.initialized() && !scriptEnvironment.initializing()) {
			scriptRunner.submit(requiredModule, scriptEnvironment);
		}
		// otherwise, just restart with the exports, things are already in progress
		else {
			restartInProgress(requiredModule, scriptEnvironment);
		}
		
		// this allows us to automatically implement the circular dependency
		// specification - if the module is already in progress, then we just
		// return the exports right away and they will get populated as things
		// keep running
	}

	private void restartInProgress(final RequiredModule requiredModule, ModuleScriptEnvironment scriptEnvironment) {
		scriptRunner.submit(
			"restarting [" + requiredModule.baseName() + "] with in-progress module [" + requiredModule.identifier() + "]",
			requiredModule.parentContext(),
			requiredModule.pendingKey(),
			scriptEnvironment.exports()
		);
	}
}
