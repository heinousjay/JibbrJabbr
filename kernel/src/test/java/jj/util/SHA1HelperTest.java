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
package jj.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import jj.util.SHA1Helper;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class SHA1HelperTest {

	/**
	 * This test is late but it exercises a bug i caused that
	 * made for all sorts of blowing up so this is just to
	 * ensure it never happens again.  otherwise it's a really
	 * simple class
	 */
	@Test
	public void testStupidBugICaused() {
		final String string1 = "string";
		final String string2 = "string";
		
		assertThat(SHA1Helper.keyFor(string1), is(SHA1Helper.keyFor(string2)));
		assertThat(SHA1Helper.keyFor(string1,string1,string1), is(SHA1Helper.keyFor(string2,string2,string2)));
		assertThat(SHA1Helper.keyFor(string1), is(not(SHA1Helper.keyFor(string2, string2))));
	}

}
