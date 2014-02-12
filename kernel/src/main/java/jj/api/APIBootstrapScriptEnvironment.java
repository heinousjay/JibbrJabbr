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
package jj.api;

import java.io.IOException;

import javax.inject.Provider;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

import jj.execution.IOThread;
import jj.logging.EmergencyLogger;
import jj.resource.ResourceCacheKey;
import jj.resource.script.RootScriptEnvironment;
import jj.script.AbstractScriptEnvironment;
import jj.script.RhinoContext;

/**
 * adapts the API into a script environment so it can be bootstrapped
 * via the resource system
 * 
 * @author jason
 *
 */
public class APIBootstrapScriptEnvironment extends AbstractScriptEnvironment implements RootScriptEnvironment {

	@Override
	public Scriptable scope() {
		return global;
	}

	@Override
	public Script script() {
		// should return the script that does the requires,
		// which i guess it needs to build by reading the directory
		return null;
	}

	@Override
	public String sha1() {
		return null;
	}

	@Override
	public String scriptName() {
		return "JJ API";
	}

	@Override
	public String baseName() {
		return "JJ API";
	}

	@Override
	public String uri() {
		return "JJ API";
	}

	@Override
	@IOThread
	public boolean needsReplacing() throws IOException {
		// replacing happens from dependencies
		return false;
	}
	
	private final ScriptableObject global;

	/**
	 * @param cacheKey
	 * @param publisher
	 * @param contextMaker
	 */
	APIBootstrapScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final @EmergencyLogger Logger logger,
		final Provider<RhinoContext> contextProvider,
		final API api
	) {
		super(cacheKey, contextProvider);

		
		global = api.global();
	}
}
