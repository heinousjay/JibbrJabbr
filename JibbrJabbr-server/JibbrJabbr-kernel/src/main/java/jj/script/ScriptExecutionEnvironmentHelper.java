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

import jj.execution.JJExecutors;
import jj.execution.ScriptThread;
import jj.resource.ResourceFinder;
import jj.resource.document.ScriptResource;
import jj.resource.document.ScriptResourceType;

/**
 * API for script execution environment maintenance
 * 
 * @author jason
 *
 */
@Singleton
class ScriptExecutionEnvironmentHelper {
	
	private final ResourceFinder finder;
	private final ScriptExecutionEnvironments scriptExecutionEnvironments;
	private final ScriptExecutionEnvironmentCreator creator;
	private final JJExecutors executors;
	
	@Inject
	ScriptExecutionEnvironmentHelper(
		final ResourceFinder finder,
		final ScriptExecutionEnvironments scriptExecutionEnvironments,
		final ScriptExecutionEnvironmentCreator creator,
		final JJExecutors executors
	) {
		this.finder = finder;
		this.scriptExecutionEnvironments = scriptExecutionEnvironments;
		this.creator = creator;
		this.executors = executors;
	}
	
	private ScriptResource moduleScript(final String moduleIdentifier) {
		return finder.findResource(ScriptResource.class, ScriptResourceType.Module.suffix(moduleIdentifier));
	}
	
	private ScriptResource clientScript(final String baseName) {
		return finder.findResource(ScriptResource.class, ScriptResourceType.Client.suffix(baseName));
	}
	
	private ScriptResource sharedScript(final String baseName) {
		return finder.findResource(ScriptResource.class, ScriptResourceType.Shared.suffix(baseName));
	}
	
	private ScriptResource serverScript(final String baseName) {
		return finder.findResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
	}
	
	
	public boolean isObselete(final DocumentScriptExecutionEnvironment scriptExecutionEnvironment) {
		final String baseName = scriptExecutionEnvironment.baseName();
		
		// cache rules mean object equality works
		return (scriptExecutionEnvironment.clientScriptResource() != clientScript(baseName)) ||
			(scriptExecutionEnvironment.sharedScriptResource() != sharedScript(baseName)) ||
			(scriptExecutionEnvironment.serverScriptResource() != serverScript(baseName));
	}
	
	public boolean isObselete(final ModuleScriptExecutionEnvironment scriptExecutionEnvironment) {
		return scriptExecutionEnvironment.scriptResource() != 
			finder.findResource(scriptExecutionEnvironment.scriptResource());
	}
	
	/**
	 * returns the most current script execution environment available for the given
	 * baseName, creating a new one if needed
	 * @param baseName
	 * @return
	 */
	@ScriptThread
	public DocumentScriptExecutionEnvironment scriptExecutionEnvironmentFor(final String baseName) {
		
		assert (executors.isScriptThreadFor(baseName)) : "";
		
		ScriptExecutionEnvironment scriptExecutionEnvironment = scriptExecutionEnvironments.get(baseName);
		DocumentScriptExecutionEnvironment candidate = 
			scriptExecutionEnvironment instanceof DocumentScriptExecutionEnvironment ? 
				(DocumentScriptExecutionEnvironment)scriptExecutionEnvironment :
				null;
				
		if (candidate != null) {
			if (isObselete(candidate)) {
				DocumentScriptExecutionEnvironment next = creator.createScriptExecutionEnvironment(
					clientScript(baseName), 
					sharedScript(baseName), 
					serverScript(baseName), 
					baseName
				);
				
				if (!scriptExecutionEnvironments.replace(baseName, candidate, next)) {
					throw new AssertionError("multiple threads are attempting to manipulate a single script execution environment");
				}
				
				candidate = next;
			}
		} else {
			candidate = newScriptExecutionEnvironmentFor(baseName);
		}
		return candidate;
	}
	
	@ScriptThread
	private DocumentScriptExecutionEnvironment newScriptExecutionEnvironmentFor(final String baseName) {
		
		ScriptResource serverScript = serverScript(baseName);
		
		DocumentScriptExecutionEnvironment newExecutionEnvironment = serverScript == null ? 
			null : 
			creator.createScriptExecutionEnvironment(
				clientScript(baseName),
				sharedScript(baseName),
				serverScript,
				baseName
			);
		
		if (newExecutionEnvironment != null && 
			scriptExecutionEnvironments.putIfAbsent(baseName, newExecutionEnvironment) != null) {
			throw new AssertionError("multiple threads are attempting to manipulate a single script execution environment");
		}
		
		return newExecutionEnvironment;
	}
	
	@ScriptThread
	public ModuleScriptExecutionEnvironment scriptExecutionEnvironmentFor(final String baseName, final String moduleIdentifier) {
		final String key = ModuleScriptExecutionEnvironment.makeKey(baseName, moduleIdentifier);
		
		ScriptExecutionEnvironment scriptExecutionEnvironment = scriptExecutionEnvironments.get(key);
		ModuleScriptExecutionEnvironment candidate = 
			scriptExecutionEnvironment instanceof ModuleScriptExecutionEnvironment ? 
				(ModuleScriptExecutionEnvironment)scriptExecutionEnvironment :
				null;
		
		if (candidate != null && moduleScript(moduleIdentifier) != null) {
			if (isObselete(candidate)) {
				ModuleScriptExecutionEnvironment newExecutionEnvironment = 
					creator.createScriptExecutionEnvironment(moduleScript(moduleIdentifier), moduleIdentifier, baseName);
				
				if (!scriptExecutionEnvironments.replace(key, candidate, newExecutionEnvironment)) {
					throw new AssertionError("multiple threads are attempting to manipulate a single script execution environment");
				}
				
				candidate = newExecutionEnvironment;
			}
		} else {
			candidate = newScriptExecutionEnvironmentFor(key, baseName, moduleIdentifier);
		}
		
		return candidate;
	}
	
	@ScriptThread
	private ModuleScriptExecutionEnvironment newScriptExecutionEnvironmentFor(final String key, final String baseName, final String moduleIdentifier) {
		
		ScriptResource script = moduleScript(moduleIdentifier);
		
		ModuleScriptExecutionEnvironment newExecutionEnvironment = 
			creator.createScriptExecutionEnvironment(script, moduleIdentifier, baseName);
		
		if (newExecutionEnvironment != null && scriptExecutionEnvironments.putIfAbsent(key, newExecutionEnvironment) != null) {
			throw new AssertionError("multiple threads are attempting to manipulate a single script execution environment");
		}
		
		return newExecutionEnvironment;
	}
}
