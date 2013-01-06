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

import jj.resource.ScriptResource;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * script bundle that represents the result of
 * a require() call inside the script host.
 * these probably need to be keyed by the baseName +
 * the path the module, i'm pretty sure i want
 * independent instances per page-level script
 * @author jason
 *
 */
public class ModuleScriptBundle implements ScriptBundle {
	
	public static String makeKey(final String baseName, final String moduleIdentifier) {
		return baseName + ":" + moduleIdentifier;
	}
	
	private final ScriptResource scriptResource;
	private final Scriptable local;
	private final Script script;
	private final Scriptable exports;
	private final String moduleIdentifier;
	private final String baseName;
	
	private boolean initialized = false;
	
	ModuleScriptBundle(
		final ScriptResource scriptResource,
		final Scriptable local,
		final Script script,
		final Scriptable exports,
		final String moduleIdentifier,
		final String baseName
	) {
		this.scriptResource = scriptResource;
		this.local = local;
		this.script = script;
		this.exports = exports;
		this.moduleIdentifier = moduleIdentifier;
		this.baseName = baseName;
	}

	@Override
	public Scriptable scope() {
		return local;
	}

	@Override
	public Script script() {
		return script;
	}

	@Override
	public String sha1() {
		return scriptResource.sha1();
	}

	public String moduleIdentifier() {
		return moduleIdentifier;
	}
	
	@Override
	public String baseName() {
		return baseName;
	}

	@Override
	public boolean initialized() {
		return initialized;
	}

	public void initialized(final boolean initialized) {
		this.initialized = initialized || this.initialized;
	}
	
	public Scriptable exports() {
		return exports;
	}
	
	public ScriptResource scriptResource() {
		return scriptResource;
	}
}
