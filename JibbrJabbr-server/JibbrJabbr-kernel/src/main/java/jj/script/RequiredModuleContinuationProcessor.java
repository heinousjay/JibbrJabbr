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
import jj.execution.JJExecutors;
import jj.resource.ResourceFinder;
import jj.resource.document.ScriptResource;
import jj.resource.document.ScriptResourceType;
import jj.resource.spec.SpecResource;

/**
 * @author jason
 *
 */
@Singleton
class RequiredModuleContinuationProcessor implements ContinuationProcessor {
	
	private final CurrentScriptContext context;
	
	private final JJExecutors executors;
	
	private final ScriptRunnerInternal scriptRunner;
	
	private final ResourceFinder finder;
	
	private final ScriptExecutionEnvironmentFinder scriptFinder;
	
	@Inject
	RequiredModuleContinuationProcessor(
		final CurrentScriptContext context,
		final JJExecutors executors,
		final ScriptRunnerInternal scriptRunner,
		final ResourceFinder finder,
		final ScriptExecutionEnvironmentFinder scriptFinder
	) {
		this.context = context;
		this.executors = executors;
		this.scriptRunner = scriptRunner;
		this.finder = finder;
		this.scriptFinder = scriptFinder;
	}
	
	private void loadScript(final RequiredModule requiredModule) {
		
		executors.execute(
			new IOTask("loading module [" + requiredModule.identifier() + "] from [" + context.baseName() + "]") {
			
				@Override
				public void run() {
					ScriptResource scriptResource = 
						finder.loadResource(ScriptResource.class, ScriptResourceType.Module.suffix(requiredModule.identifier()));
					
					// at this point do we need to check if we got scooped? inside
					// the script thread makes more sense really, if we check here
					// then we are potentially contending for the stores but if we
					// check inside a script thread then strict ordering will happen,
					// so submit needs to check if it's doing useless work at the
					// beginning and just restart and then we just wasted a little i/o
					// time, no biggy
					
					if (scriptResource != null) {
						// make sure the associated spec, if any, is also loaded
						finder.loadResource(SpecResource.class, scriptResource.baseName());
						scriptRunner.submit(requiredModule);
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
		
		ScriptResource scriptResource = 
			finder.findResource(ScriptResource.class, ScriptResourceType.Module.suffix(requiredModule.identifier()));
		
		ModuleScriptExecutionEnvironment scriptExecutionEnvironment = 
			scriptFinder.forBaseNameAndModuleIdentifier(context.baseName(), requiredModule.identifier());
		
		// decision 1: do we need i/o?
		if (scriptResource == null) {
			loadScript(requiredModule);
		}	
		// decision 2: do we need to reinitialize the execution environment?
		else if (!scriptResource.sha1().equals(scriptExecutionEnvironment.sha1())) {
			scriptRunner.submit(requiredModule);
		}
		// otherwise, just restart with the exports, things are already in progress
		else {
			scriptRunner.submit(
				"restarting [" + requiredModule.baseName() + "] with in-progress module [" + requiredModule.identifier() + "]",
				requiredModule.parentContext(),
				requiredModule.pendingKey(),
				scriptExecutionEnvironment.exports()
			);
		}
		
		// this allows us to automatically implement the circular dependency
		// specification - if the module is already in progress, then we just
		// return the exports right away and they will get populated as things
		// keep running
	}
}
