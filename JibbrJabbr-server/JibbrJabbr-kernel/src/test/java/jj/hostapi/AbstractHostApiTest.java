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

import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jj.JJ;

import org.mozilla.javascript.EvaluatorException;

/**
 * @author jason
 *
 */
abstract class AbstractHostApiTest {

	protected final String scriptName() {
		return getClass().getSimpleName() + ".js";
	}

	protected final String script() throws Exception {
		Path me = Paths.get(JJ.uri(getClass()));
		return script(me.resolveSibling(scriptName()));
	}

	protected final String script(final Path scriptPath) throws Exception {
		return new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);
	}

	protected final RhinoObjectCreator makeHost(final HostObject...hostObjects) {
		Set<HostObject> result = new HashSet<>();
		Collections.addAll(result,  hostObjects);
		return new RhinoObjectCreatorImpl(result);
	}

	protected final void basicExecution(final RhinoObjectCreator host) throws Exception {
		try {
			host.context().evaluateString(host.global(), script(), scriptName(), 1, null);
		} catch (EvaluatorException ee) {
			fail(ee.getMessage());
		}
	}

}