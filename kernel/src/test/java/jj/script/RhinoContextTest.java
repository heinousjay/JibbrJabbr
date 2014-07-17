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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mozilla.javascript.RhinoContextTestHelper.*;

import jj.event.MockPublisher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Script;

/**
 * @author jason
 *
 */
public class RhinoContextTest {
	
	MockPublisher publisher;
	RhinoContext context;
	
	@Before
	public void before() {
		context = new RhinoContext(publisher);
	}
	
	@After
	public void after() {
		context.close();
	}

	@Test
	public void testWithContinuations() {
		// target verification here is that we are set for language level 1.8
		// and optimization level -1 (for continuations)
		Script script = context.compileString("function() {}", "testWithContinuations");
		
		assertTrue(isInterpretedScript(script));
		assertThat(getLanguageVersion(script), is(180));
	}
	
	@Test
	public void testWithoutContinuations() {
		
		Script script = context.withoutContinuations().compileString("function() {}", "testWithoutContinuations");
		
		assertFalse(isInterpretedScript(script)); // the only real change here!
	}

	// damn near everything is just a pass-through.  not sure what i actually care to test here
}
