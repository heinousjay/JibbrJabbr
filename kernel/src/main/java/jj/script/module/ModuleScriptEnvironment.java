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


import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;

import java.io.IOException;

import javax.inject.Inject;

import jj.resource.Location;
import jj.resource.ResourceThread;
import jj.resource.NoSuchResourceException;
import jj.script.AbstractScriptEnvironment;
import jj.script.ChildScriptEnvironment;
import jj.script.PendingKey;
import jj.script.RhinoContext;
import jj.script.ScriptEnvironment;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * <p>
 * Provides the notion of a script module, which is the standard unit of execution
 * in the JibbrJabbr system. A module must belong to a parent script environment of
 * some sort, which provides the top-level execution semantics, but in principle, all
 * modules are interchangable.
 * 
 * <p>
 * Modules are identified by a tuple of identifier and parent, so the same script loaded
 * from two different parents will have independent scopes
 * 
 * <p>
 * Modules can be either executable scripts, which can assign to the provided module.exports
 * or exports variables within their scopes, or they can be a single serialized JSON object,
 * which will be exported directly.
 * 
 * <p>
 * Modules that are provided by the server are prepending with jj/ to identify them.  This means if
 * you have a directory in your application named jj, you will have weird times getting to modules
 * in that directory, so don't do it.
 * 
 * <p>
 * Otherwise, name resolution works like directory resolution, relative paths are resolved
 * against the current module identifier much like changing directories in a shell.  paths that
 * start with / are resolved from the application root.  it is possible to navigate "below" the
 * root with enough .. units in the module path, but that may not stick around so don't get
 * too excited about it.
 * 
 * @author jason
 *
 */
public class ModuleScriptEnvironment extends AbstractScriptEnvironment implements ChildScriptEnvironment {
	
	public static final String API_PREFIX = "jj/";
	
	private final RequiredModule requiredModule;
	
	// the key to restarting whatever included this.  gets removed on first read and is null forever after
	// maybe not a good spot? it's not necessarily the same as the overall root environment
	// does not need to be volatile because this interaction is guaranteed to be from one thread
	private PendingKey pendingKey;
	
	private final ScriptableObject scope;
	
	private final Script script;
	
	private final String sha1;
	
	@Inject
	ModuleScriptEnvironment(
		final Dependencies dependencies,
		final RequiredModule requiredModule
	) {
		super(dependencies);

		String moduleIdentifier = name;
		
		Location base = Base;
		if (name.startsWith(API_PREFIX)) {
			base = APIModules;
			moduleIdentifier = name.substring(API_PREFIX.length());
		}
		
		this.requiredModule = requiredModule;
		
		pendingKey = requiredModule.pendingKey();
		
		assert requiredModule.parent().alive(): "cannot require a module for a dead parent";
		
		// we look for a script and a JSON file, with script taking precedence
		
		ScriptResource scriptResource = resourceFinder.loadResource(ScriptResource.class, base, moduleIdentifier + ".js");
		JSONResource   jsonResource   = resourceFinder.loadResource(JSONResource.class, base, moduleIdentifier + ".json");
		
		if (scriptResource == null && jsonResource == null) {
			
			throw new NoSuchResourceException(getClass(), moduleIdentifier);
			
		} else if (scriptResource == null) {
			
			jsonResource.addDependent(requiredModule.parent());
			sha1 = jsonResource.sha1();
			scope = configureModuleObjects(moduleIdentifier, createChainedScope(requiredModule.parent().global()));
			script = null;
			
			try (RhinoContext context = contextProvider.get()) {
				Scriptable module = (Scriptable)context.evaluateString(scope, "module", "");
				module.put("exports", module, jsonResource.contents());
			}
			
		} else {
			
			scriptResource.addDependent(requiredModule.parent());
			sha1 = scriptResource.sha1();
			scope = configureTimers(configureModuleObjects(moduleIdentifier, createChainedScope(requiredModule.parent().global())));
			script = scriptResource.script();
			
			if (scriptResource.base() == APIModules) { 
				configureInjectFunction(scope);
			}
		}
		
		// we need to reload each other on changes
		requiredModule.parent().addDependent(this);
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
		return name + ".js";
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
	public PendingKey initializationContinuationPendingKey() {
		PendingKey result = pendingKey;
		pendingKey = null;
		return result;
	}

	@Override
	@ResourceThread
	public boolean needsReplacing() throws IOException {
		// we are obselete when our script is
		// but we don't listen as a dependent, our parent
		// does. we'll get reloaded anyway
		return false;
	}
}
