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
package jj;

import static jj.MockJJExecutors.ThreadType.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class MockJJExecutorsTest {
	
	
	@Test
	public void testThreadTypeReplies() {
		MockJJExecutors e = new MockJJExecutors();
		e.addThreadTypes(ScriptThread, 2);
		e.addThreadTypes(IOThread, 2);
		
		assertThat(e.isScriptThread(), is(true));
		assertThat(e.isScriptThread(), is(true));
		
		boolean worked = true;
		try {
			e.assertThreadTypesEmpty();
			worked = false;
		} catch (AssertionError er) {}
		
		assertTrue("didn't assert emptiness", worked);
		
		assertThat(e.isIOThread(), is(true));
		assertThat(e.isIOThread(), is(true));
		
		e.assertThreadTypesEmpty();
		
		e.addThreadTypes(ScriptThread, 2);
		e.addThreadTypes(IOThread, 2);

		assertThat(e.isIOThread(), is(false));
		assertThat(e.isIOThread(), is(false));
		assertThat(e.isScriptThread(), is(false));
		assertThat(e.isScriptThread(), is(false));
		
		e.assertThreadTypesEmpty();
		
		e.addThreadTypes(IOThread, 2);
		
		worked = true;
		try {
			e.assertThreadTypesEmpty();
			worked = false;
		} catch (AssertionError er) {}
		
		assertTrue("didn't assert emptiness", worked);
	}
	
	@Test
	public void testThreadTypesSequence() {
		MockJJExecutors e = new MockJJExecutors();
		
		e.scriptExecutorFor("");
		e.ioExecutor();
		e.scriptExecutorFactory().executorFor("");
		
		e.assertExecutorSequence(ScriptThread, IOThread, ScriptThread);
		
		boolean worked = true;
		try {
			e.assertExecutorSequence(ScriptThread, ScriptThread, ScriptThread, IOThread, ScriptThread);
			worked = false;
		} catch (AssertionError error) {}
		
		assertThat("didn't assert correctly", worked, is(true));
		
		// should still have the sequence
		e.assertExecutorSequence(ScriptThread, IOThread, ScriptThread);
	}
}
