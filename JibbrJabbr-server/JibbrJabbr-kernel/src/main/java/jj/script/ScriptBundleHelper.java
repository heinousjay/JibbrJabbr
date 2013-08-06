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

import jj.execution.JJExecutors;
import jj.execution.ScriptThread;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;

/**
 * API for script bundle maintenance
 * 
 * @author jason
 *
 */
@Singleton
class ScriptBundleHelper {
	
	private final ResourceFinder finder;
	private final ScriptBundles scriptBundles;
	private final ScriptBundleCreator creator;
	private final JJExecutors executors;
	
	@Inject
	ScriptBundleHelper(
		final ResourceFinder finder,
		final ScriptBundles scriptBundles,
		final ScriptBundleCreator creator,
		final JJExecutors executors
	) {
		this.finder = finder;
		this.scriptBundles = scriptBundles;
		this.creator = creator;
		this.executors = executors;
	}
	
	private ScriptResource moduleScript(final String baseName, final String moduleIdentifier) {
		String path = Paths.get(baseName).resolveSibling(moduleIdentifier).toString();
		return finder.findResource(ScriptResource.class, ScriptResourceType.Module.suffix(path));
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
	
	
	public boolean isObselete(final AssociatedScriptBundle scriptBundle) {
		final String baseName = scriptBundle.baseName();
		
		// cache rules mean object equality works
		return (scriptBundle.clientScriptResource() != clientScript(baseName)) ||
			(scriptBundle.sharedScriptResource() != sharedScript(baseName)) ||
			(scriptBundle.serverScriptResource() != serverScript(baseName));
	}
	
	public boolean isObselete(final ModuleScriptBundle scriptBundle) {
		return scriptBundle.scriptResource() != 
			finder.findResource(scriptBundle.scriptResource());
	}
	
	/**
	 * returns the most current script bundle available for the given
	 * baseName, creating a new bundle if needed
	 * @param baseName
	 * @return
	 */
	@ScriptThread
	public AssociatedScriptBundle scriptBundleFor(final String baseName) {
		
		assert (executors.isScriptThreadFor(baseName)) : "";
		
		ScriptBundle scriptBundle = scriptBundles.get(baseName);
		AssociatedScriptBundle candidate = 
			scriptBundle instanceof AssociatedScriptBundle ? 
				(AssociatedScriptBundle)scriptBundle :
				null;
				
		if (candidate != null) {
			if (isObselete(candidate)) {
				AssociatedScriptBundle next = creator.createScriptBundle(
					clientScript(baseName), 
					sharedScript(baseName), 
					serverScript(baseName), 
					baseName
				);
				
				if (!scriptBundles.replace(baseName, candidate, next)) {
					throw new AssertionError("multiple threads are attempting to manipulate a single script bundle");
				}
				
				candidate = next;
			}
		} else {
			candidate = newScriptBundleFor(baseName);
		}
		return candidate;
	}
	
	@ScriptThread
	private AssociatedScriptBundle newScriptBundleFor(final String baseName) {
		
		ScriptResource serverScript = serverScript(baseName);
		
		AssociatedScriptBundle newBundle = serverScript == null ? null : creator.createScriptBundle(
			clientScript(baseName),
			sharedScript(baseName),
			serverScript,
			baseName
		);
		
		if (newBundle != null && scriptBundles.putIfAbsent(baseName, newBundle) != null) {
			throw new AssertionError("multiple threads are attempting to manipulate a single script bundle");
		}
		
		return newBundle;
	}
	
	@ScriptThread
	public ModuleScriptBundle scriptBundleFor(final String baseName, final String moduleIdentifier) {
		final String key = ModuleScriptBundle.makeKey(baseName, moduleIdentifier);
		
		ScriptBundle scriptBundle = scriptBundles.get(key);
		ModuleScriptBundle candidate = 
			scriptBundle instanceof ModuleScriptBundle ? 
				(ModuleScriptBundle)scriptBundle :
				null;
		
		if (candidate != null && moduleScript(baseName, moduleIdentifier) != null) {
			if (isObselete(candidate)) {
				ModuleScriptBundle newBundle = creator.createScriptBundle(moduleScript(baseName, moduleIdentifier), moduleIdentifier, baseName);
				
				if (!scriptBundles.replace(key, candidate, newBundle)) {
					throw new AssertionError("multiple threads are attempting to manipulate a single script bundle");
				}
				
				candidate = newBundle;
			}
		} else {
			candidate = newScriptBundleFor(key, baseName, moduleIdentifier);
		}
		
		return candidate;
	}
	
	@ScriptThread
	private ModuleScriptBundle newScriptBundleFor(final String key, final String baseName, final String moduleIdentifier) {
		
		ScriptResource script = moduleScript(baseName, moduleIdentifier);
		
		ModuleScriptBundle newBundle = creator.createScriptBundle(script, moduleIdentifier, baseName);
		
		if (newBundle != null && scriptBundles.putIfAbsent(key, newBundle) != null) {
			throw new AssertionError("multiple threads are attempting to manipulate a single script bundle");
		}
		
		return newBundle;
	}
}
