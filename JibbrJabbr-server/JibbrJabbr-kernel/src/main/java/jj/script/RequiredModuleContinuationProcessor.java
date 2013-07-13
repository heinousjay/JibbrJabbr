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

import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.RequiredModuleException;
import jj.execution.JJExecutors;
import jj.execution.JJRunnable;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;

/**
 * @author jason
 *
 */
@Singleton
class RequiredModuleContinuationProcessor implements ContinuationProcessor {
	
	private final CurrentScriptContext context;
	
	private final JJExecutors executors;
	
	private final ResourceFinder finder;
	
	private final ScriptBundleFinder scriptFinder;
	
	@Inject
	RequiredModuleContinuationProcessor(
		final CurrentScriptContext context,
		final JJExecutors executors,
		final ResourceFinder finder,
		final ScriptBundleFinder scriptFinder
	) {
		this.context = context;
		this.executors = executors;
		this.finder = finder;
		this.scriptFinder = scriptFinder;
	}

	@Override
	public ContinuationType type() {
		return ContinuationType.RequiredModule;
	}
	
	private void loadScript(final RequiredModule requiredModule) {
		
		final String baseName = context.baseName();
		final String path = Paths.get(baseName).resolveSibling(requiredModule.identifier()).toString();
		
		executors.ioExecutor().submit(
			new JJRunnable("loading module [" + requiredModule.identifier() + "] from [" + baseName + "]") {
			
				@Override
				public void run() {
					ScriptResource scriptResource = 
						finder.loadResource(ScriptResource.class, path, ScriptResourceType.Module);
					
					// at this point do we need to check if we got scooped? inside
					// the script thread makes more sense really, if we check here
					// then we are potentially contending for the stores but if we
					// check inside a script thread then strict ordering will happen,
					// so submit needs to check if it's doing useless work at the
					// beginning and just restart and then we just wasted a little i/o
					// time, no biggy
					
					if (scriptResource != null) {
						executors.scriptRunner().submit(requiredModule);
					} else {
						resumeContinuationAfterError(requiredModule, baseName, new RequiredModuleException(requiredModule.identifier()));
					}
				}
			}
		);
	}
	
	private void resumeContinuationAfterError(
		final RequiredModule require,
		final String baseName,
		final Object result
	) {
		
		executors.scriptExecutorFor(baseName).submit(
			new JJRunnable("required module " + require.identifier() + " error result in [" + baseName + "]") {
			
				@Override
				public void run() {
					
					context.restore(require.parentContext());
					
					try {
						executors.scriptRunner().restartAfterContinuation(require.pendingKey(), result);
					} finally {
						context.end();
					}
				}
			}
		);
	}

	@Override
	public void process(final ContinuationState continuationState) {
		final RequiredModule requiredModule = continuationState.requiredModule();
		
		ScriptResource scriptResource = 
			finder.findResource(ScriptResource.class, requiredModule.identifier(), ScriptResourceType.Module);
		
		ModuleScriptBundle scriptBundle = 
			scriptFinder.forBaseNameAndModuleIdentifier(context.baseName(), requiredModule.identifier());
		
		// decision 1: do we need i/o?
		if (scriptResource == null) {
			loadScript(requiredModule);
		}	
		// decision 2: do we need to reinitialize the bundle?
		else if (!scriptResource.sha1().equals(scriptBundle.sha1())) {
			executors.scriptRunner().submit(requiredModule);
		}
		// otherwise, just restart with the exports, we're already
		// in the right place
		else {
			executors.scriptRunner().restartAfterContinuation(
				requiredModule.pendingKey(),
				scriptBundle.exports()
			);
		}
		
		// this allows us to automatically implement the circular dependency
		// specification - if the module is already in progress, then we just
		// return the exports right away and they will get populated as things
		// keep running
	}
}
