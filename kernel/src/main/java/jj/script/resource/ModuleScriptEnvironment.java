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
package jj.script.resource;


import static jj.configuration.AppLocation.*;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.EngineAPI;
import jj.resource.AbstractResource;
import jj.resource.ResourceThread;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
import jj.script.AbstractScriptEnvironment;
import jj.script.ChildScriptEnvironment;
import jj.script.ContinuationPendingKey;
import jj.script.RhinoContext;
import jj.script.ScriptEnvironment;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@Singleton
public class ModuleScriptEnvironment extends AbstractScriptEnvironment implements ChildScriptEnvironment {
	
	private final String moduleIdentifier;
	
	private final RequiredModule requiredModule;
	
	// the key to restarting whatever included this.  gets removed on first read and is null forever after
	// maybe not a good spot? it's not necessarily the same as the overall root environment
	private ContinuationPendingKey pendingKey;
	
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
		final Dependencies dependencies,
		final String moduleIdentifier,
		final RequiredModule requiredModule,
		final EngineAPI api,
		final ResourceFinder resourceFinder,
		final InjectorBridgeFunction injectorBridge
	) {
		super(dependencies);
		
		this.moduleIdentifier = moduleIdentifier;
		
		this.requiredModule = requiredModule;
		
		pendingKey = requiredModule.pendingKey();
		
		assert ((AbstractResource)requiredModule.parent()).alive(): "cannot require a module for a dead parent";
		
		scriptResource = resourceFinder.loadResource(ScriptResource.class, Base.and(Assets), scriptName());
		
		if (scriptResource == null) {
			throw new NoSuchResourceException(moduleIdentifier);
		}
		
		scriptResource.addDependent(requiredModule.parent());
		requiredModule.parent().addDependent(this);
		
		sha1 = scriptResource.sha1();
		scope = createChainedScope(api.global());
		configureModuleObjects(moduleIdentifier, scope);
		
		if (scriptResource.base() == Assets) {
			scope.defineProperty(InjectorBridgeFunction.NAME, injectorBridge, ScriptableObject.CONST);
		}
		
		try (RhinoContext context = contextProvider.get()) {
			
			script = context.compileString(scriptResource.script(), scriptName());
		
		}
	}
	

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
	public String name() {
		return moduleIdentifier;
	}

	@Override
	public String uri() {
		return "/"; // can not be loaded directly
	}
	
	@Override
	protected Object[] creationArgs() {
		return new Object[] { requiredModule };
	}
	
	@Override
	public ScriptEnvironment parent() {
		return requiredModule.parent();
	}

	@Override
	public ContinuationPendingKey pendingKey() {
		ContinuationPendingKey result = pendingKey;
		pendingKey = null;
		return result;
	}

	@Override
	@ResourceThread
	public boolean needsReplacing() throws IOException {
		// we are obselete when our script is
		// but we don't listen as a dependent, our parent
		// does. we'll get reloaded anyway
		return scriptResource.needsReplacing();
	}
}
