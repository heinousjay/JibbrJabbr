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
package jj.jasmine;

import static jj.configuration.resolution.AppLocation.*;

import java.io.IOException;

import javax.inject.Inject;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoaded;
import jj.script.AbstractScriptEnvironment;
import jj.script.Global;
import jj.script.RhinoContext;
import jj.script.module.RootScriptEnvironment;
import jj.script.module.ScriptResource;
import jj.util.SHA1Helper;

/**
 * <p>
 * The central object of the Jasmine integration. Coordinates a number of filesystem
 * resources, provides a reload point in the resource system, and provides an
 * environment for the script execution
 * 
 * @author jason
 *
 */
public class JasmineScriptEnvironment extends AbstractScriptEnvironment implements RootScriptEnvironment {

	private static final String JASMINE         = "jasmine";
	static final String JASMINE_JS      = JASMINE + ".js";
	static final String JASMINE_BOOT_JS = JASMINE + "-boot.js";
	static final String JASMINE_RUN_JS  = JASMINE + "-run.js";

	private final ScriptableObject scope;
	
	private final ScriptableObject global;
	
	private final String sha1;
	
	private final String uri;
	
	private final ScriptResource jasmine;
	
	private final ScriptResource target;
	
	private final ScriptResource spec;
	
	private final ScriptResource boot;
	
	private final ScriptResource runner;
	
	private final ResourceLoaded resourceLoaded;
	
	@Inject
	JasmineScriptEnvironment(
		final Dependencies dependencies,
		final @Global ScriptableObject global,
		final ResourceFinder resourceFinder,
		final ResourceLoaded resourceLoaded // the original event that caused us to get loaded
	) {
		super(dependencies);
		
		this.resourceLoaded = resourceLoaded;
		
		this.global = global;
		
		// we need some scripts to be happy
		target  = resourceFinder.loadResource(ScriptResource.class, resourceLoaded.base, resourceLoaded.name);
		spec    = resourceFinder.loadResource(ScriptResource.class, resourceLoaded.base, name);
		
		// target is unlikely to be null, but maybe it got deleted while we were getting created
		if (target == null || spec == null) {
			throw new NoSuchResourceException(JasmineScriptEnvironment.class, name);
		}
		
		// if we have a reason to load stuff, load stuff!
		
		jasmine = resourceFinder.loadResource(ScriptResource.class, Assets, JASMINE_JS);
		boot    = resourceFinder.loadResource(ScriptResource.class, Assets, JASMINE_BOOT_JS);
		runner  = resourceFinder.loadResource(ScriptResource.class, Assets, JASMINE_RUN_JS);
		
		assert jasmine != null : "can't find the jasmine script";
		assert boot != null    : "can't find the jasmine-boot script";
		assert runner != null  : "can't find the jasmine-run script";
		
		// okay, we have all the things we need, now we can bother finishing up
		// need to configure different require.  also need different inject.
		// to allow things to be mocked correctly
		scope = configureModuleObjects(JASMINE, createChainedScope(global), "$$realRequire");
		
		configureInjectFunction(configureTimers(scope), "$$realInject");
		
		// and we need to pre-execute the jasmine script into our scope, because requiring it
		// makes it use the wrong global, and therefore timers can't be simulated
		try (RhinoContext context = contextProvider.get()) {
			context.executeScript(jasmine.script(), scope);
		}
		
		// do the dependency dance! gotta hook up to all our business
		target.addDependent(this);
		spec.addDependent(this);
		jasmine.addDependent(this);
		boot.addDependent(this);
		runner.addDependent(this);
		
		sha1 = SHA1Helper.keyFor(target.sha1(), spec.sha1(), jasmine.sha1(), boot.sha1(), runner.sha1());
		
		uri = "/" + sha1 + "/" + name;
	}
	
	@Override
	protected Object[] creationArgs() {
		return new Object[] { resourceLoaded };
	}

	@Override
	public Scriptable scope() {
		return scope;
	}
	
	@Override
	public ScriptableObject global() {
		return global;
	}

	@Override
	public Script script() {
		return boot.script();
	}
	
	ScriptResource spec() {
		return spec;
	}
	
	Script specScript() {
		return spec.script();
	}
	
	ScriptResource target() {
		return target;
	}
	
	Script targetScript() {
		return target.script();
	}
	
	Script runnerScript() {
		return runner.script();
	}

	@Override
	public String scriptName() {
		return JASMINE;
	}

	@Override
	public String uri() {
		return uri;
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public boolean needsReplacing() throws IOException {
		// driven by the underlying scripts
		return false;
	}
	
	@Override
	protected boolean removeOnReload() {
		// and with this, we magically rerun any time there
		// are changes to any script we use
		return false;
	}
}
