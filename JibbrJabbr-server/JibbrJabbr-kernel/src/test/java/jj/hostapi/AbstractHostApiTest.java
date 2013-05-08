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
package jj.hostapi;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jj.JJ;

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
abstract class AbstractHostApiTest {
	
	private static final String RHINO_UNIT = "rhinoUnitUtil.js";
	
	private final String rhinoUnit() throws Exception {
		Path me = Paths.get(JJ.uri(AbstractHostApiTest.class));
		return readPath(me.resolveSibling(RHINO_UNIT));
	}
	
	private final class RhinoUnitHostObject extends BaseFunction implements HostObject, ContributesScript {

		private static final long serialVersionUID = 1L;

		@Override
		public String script() {
			try {
				return rhinoUnit();
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
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean readonly() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean permanent() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean dontenum() {
			// TODO Auto-generated method stub
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

	protected final RhinoObjectCreator makeHost(final HostObject...hostObjects) throws Exception {
		Set<HostObject> hostObjectSet = new HashSet<>();
		Collections.addAll(hostObjectSet,  hostObjects);
		hostObjectSet.add(new RhinoUnitHostObject());
		return new RhinoObjectCreatorImpl(hostObjectSet);
	}

	protected final void basicExecution(final RhinoObjectCreator host) throws Exception {
		try {
			host.context().evaluateString(host.global(), script(), scriptName(), 1, null);
		} catch (EvaluatorException ee) {
			fail(ee.getMessage());
		}
	}
}
