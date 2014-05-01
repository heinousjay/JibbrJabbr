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
package jj.webdriver.panel;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import jj.webdriver.panel.ByStack;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class ByStackTest {

	@Test
	public void test() {
		ByStack b = new ByStack();
		
		
		assertThat(b.resolve("hi"), is("hi"));
		ByStack b2 = b.push("first-");
		assertThat(b2.resolve("name"), is("first-name"));
		assertThat(b2.push("second-").resolve("name2"), is("first-second-name2"));
		
	}

}
