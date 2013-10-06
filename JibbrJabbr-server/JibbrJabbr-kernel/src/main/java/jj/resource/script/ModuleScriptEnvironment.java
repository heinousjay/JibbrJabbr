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

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.IOThread;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@Singleton
public class ModuleScriptEnvironment extends AbstractScriptEnvironment {


	@Override
	public Scriptable scope() {
		return scope;
	}

	@Override
	public Script script() {
		return script;
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public String scriptName() {
		return ScriptResourceType.Module.suffix(moduleIdentifier);
	}

	@Override
	public String baseName() {
		return moduleIdentifier;
	}

	@Override
	public String uri() {
		return "/"; // can not be loaded directly
	}
	
	@Override
	protected Object[] creationArgs() {
		return new Object[] { parent };
	}

	@Override
	@IOThread
	public boolean needsReplacing() throws IOException {
		// we are obselete when our script is
		// but we don't listen as a dependent, our parent
		// does. we'll get reloaded anyway
		return scriptResource.needsReplacing();
	}
	
	private final String moduleIdentifier;
	
	private final ModuleParent parent;
	
	private final ScriptableObject scope;
	
	private final ScriptResource scriptResource;
	
	private final String sha1;
	
	private final Script script;
	
	/**
	 * @param cacheKey
	 * @param publisher
	 * @param contextMaker
	 */
	@Inject
	ModuleScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final String moduleIdentifier,
		final ModuleParent parent,
		final Publisher publisher,
		final RhinoContextMaker contextMaker,
		final EngineAPI api,
		final ResourceFinder resourceFinder
	) {
		super(cacheKey, publisher, contextMaker);
		
		this.moduleIdentifier = moduleIdentifier;
		
		this.parent = new ModuleParent(resourceFinder.findResource(parent.scriptEnvironment()));
		
		scriptResource = resourceFinder.loadResource(ScriptResource.class, scriptName());
		
		if (scriptResource == null) {
			throw new NoSuchResourceException(moduleIdentifier);
		}
		
		scriptResource.addDependent(this.parent.scriptEnvironment());
		this.parent.scriptEnvironment().addDependent(this);
		
		sha1 = scriptResource.sha1();
		scope = createLocalScope(moduleIdentifier, api.global());
		
		try (RhinoContext context = contextMaker.context()) {
			
			script = context.compileString(scriptResource.script(), scriptName());
		}
	}
}