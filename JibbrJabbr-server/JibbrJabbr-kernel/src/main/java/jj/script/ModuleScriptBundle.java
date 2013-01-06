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
class ModuleScriptBundle implements ScriptBundle {
	
	private final ScriptResource scriptResource;
	private final Scriptable scope;
	private final Script script;
	private final String baseName;
	
	/**
	 * this isn't final but it must only be
	 * set once, after which this is considered initialized
	 */
	private Scriptable exports = null;
	
	ModuleScriptBundle(
		final ScriptResource scriptResource,
		final Scriptable scope,
		final Script script,
		final String baseName
	) {
		this.scriptResource = scriptResource;
		this.scope = scope;
		this.script = script;
		this.baseName = baseName;
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
		return scriptResource.sha1();
	}

	@Override
	public String baseName() {
		return baseName;
	}

	@Override
	public boolean initialized() {
		return exports != null;
	}

	public void initialized(final Scriptable exports) {
		if (this.exports == null) {
			this.exports = exports;
		}
	}
	
	public Scriptable exports() {
		return exports;
	}
}
