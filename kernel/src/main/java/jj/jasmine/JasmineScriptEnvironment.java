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

import jj.resource.ResourceFinder;
import jj.script.AbstractScriptEnvironment;
import jj.script.Global;
import jj.script.RhinoContext;
import jj.script.module.RootScriptEnvironment;
import jj.script.module.ScriptResource;

/**
 * @author jason
 *
 */
public class JasmineScriptEnvironment extends AbstractScriptEnvironment implements RootScriptEnvironment {

	private static final String JASMINE_BOOT = "jasmine-boot";
	private static final String JASMINE_BOOT_JS = JASMINE_BOOT + ".js";

	private static final String JASMINE = "jasmine";

	private final ScriptableObject scope;
	
	private final ScriptableObject global;
	
	private final String sha1;
	
	private final Script script;
	
	@Inject
	public JasmineScriptEnvironment(
		final Dependencies dependencies,
		final @Global ScriptableObject global,
		final ResourceFinder resourceFinder
	) {
		super(dependencies);
		
		this.global = global;
		
		scope = configureModuleObjects(JASMINE_BOOT, createChainedScope(global));
		
		ScriptResource boot = resourceFinder.loadResource(ScriptResource.class, APIModules, JASMINE_BOOT_JS);
		
		assert boot != null : "can't find the jasmine-boot script";
		
		boot.addDependent(this);
		
		sha1 = boot.sha1();
		
		try (RhinoContext context = contextProvider.get()) {
			script = context.compileString(boot.script(), JASMINE_BOOT_JS);
		}
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
		return script;
	}

	@Override
	public String scriptName() {
		return JASMINE_BOOT;
	}

	@Override
	public String name() {
		return JASMINE;
	}

	@Override
	public String uri() {
		return "/jasmine-runner";
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

}
