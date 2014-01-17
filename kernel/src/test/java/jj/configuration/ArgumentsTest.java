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
package jj.configuration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import jj.InitializationException;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class ArgumentsTest {

	@Test
	public void test() {
		Arguments a = new Arguments(new String[] {"jay=king","ball=fancy"});
		
		assertThat(a.get("jay"), is("king"));
		assertThat(a.get("ball"), is("fancy"));
	}
	
	@Test
	public void testError() {
		try {
			new Arguments(new String[] {"error= awesome","notballs=blocks","balls"});
			fail("should have thrown");
		} catch (InitializationException e) {
			assertThat(e.getMessage(), is("all arguments must be name=value pairs, the following were not understood\n[error= awesome, balls]"));
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}
}
