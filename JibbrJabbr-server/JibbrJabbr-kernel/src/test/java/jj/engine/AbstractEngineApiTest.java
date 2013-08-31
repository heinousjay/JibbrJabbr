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
package jj.engine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jj.CoreModule;
import jj.JJ;
import jj.engine.ContributesScript;
import jj.engine.EngineAPI;
import jj.engine.EngineAPIImpl;
import jj.engine.HostObject;
import jj.script.RealRhinoContextMaker;
import jj.script.RhinoContext;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

/**
 * provides basic utilities for building a test against an
 * API provider
 * 
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
abstract class AbstractEngineApiTest {
	
	static {
		// to get the context settings correct
		mock(CoreModule.class);
	}
	
	private static final String RHINO_UNIT = "jasmine.js";
	
	private final String rhinoUnit() throws Exception {
		Path me = Paths.get(JJ.uri(AbstractEngineApiTest.class));
		return readPath(me.resolveSibling(RHINO_UNIT));
	}
	
	private final class RhinoUnitHostObject extends BaseFunction implements HostObject, ContributesScript {

		private static final long serialVersionUID = 1L;

		@Override
		public String script() {
			try {
				return "var setTimeout = function(func) {func();};var clearTimeout = function() {};var setInterval = function() {};var clearInterval = function() {};" + rhinoUnit();
			} catch (Exception e) {
				throw new AssertionError("tests are not in order!", e);
			}
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			throw new AssertionError("not a callable function, just a hack");
		}

		@Override
		public String name() {
			return RHINO_UNIT;
		}

		@Override
		public boolean constant() {
			return false;
		}

		@Override
		public boolean readonly() {
			return false;
		}

		@Override
		public boolean permanent() {
			return false;
		}

		@Override
		public boolean dontenum() {
			return false;
		}
	}

	protected final String scriptName() {
		return getClass().getSimpleName() + ".js";
	}

	protected final String script() throws Exception {
		Path me = Paths.get(JJ.uri(getClass()));
		return readPath(me.resolveSibling(scriptName()));
	}

	protected final String readPath(final Path scriptPath) throws Exception {
		return new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);
	}

	protected final EngineAPI makeHost(final HostObject...hostObjects) throws Exception {
		Set<HostObject> hostObjectSet = new HashSet<>();
		Collections.addAll(hostObjectSet,  hostObjects);
		hostObjectSet.add(new RhinoUnitHostObject());
		return new EngineAPIImpl(new RealRhinoContextMaker(), hostObjectSet);
	}

	protected final void basicExecution(final EngineAPI host) throws Exception {
		try (RhinoContext context = new RealRhinoContextMaker().context()) {
			context.evaluateString(host.global(), script(), scriptName());
		} catch (EvaluatorException ee) {
			fail(ee.getMessage());
		}
	}
}
