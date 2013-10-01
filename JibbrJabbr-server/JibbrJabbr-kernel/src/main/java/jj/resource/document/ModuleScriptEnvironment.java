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
package jj.resource.document;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.IOThread;
import jj.resource.ResourceCacheKey;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sha1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String scriptName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String baseName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String uri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@IOThread
	protected boolean needsReplacing() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	private final ScriptableObject scope;
	
	/**
	 * @param cacheKey
	 * @param publisher
	 * @param contextMaker
	 */
	@Inject
	ModuleScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final String moduleIdentifier,
		final Publisher publisher,
		final RhinoContextMaker contextMaker,
		final EngineAPI api
	) {
		super(cacheKey, publisher, contextMaker, api);
		
		scope = createLocalScope(moduleIdentifier);
	}

}
