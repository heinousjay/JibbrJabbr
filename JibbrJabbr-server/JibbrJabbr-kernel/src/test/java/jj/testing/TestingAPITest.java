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
package jj.testing;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Paths;

import jj.JJ;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class TestingAPITest {
	
	static final String basePath;
	
	static {
		// well it's ugly, but it's portable
		basePath = Paths.get(JJ.uri(TestingAPITest.class)).getParent().getParent().getParent().toAbsolutePath().toString();
	}
	
	@Rule
	public JJTestRule jjTestRule = new JJTestRule(basePath);
	
	@Test
	public void runBasicTest() throws Exception {
		
		assertThat(
			jjTestRule.getAndWait("/index").select("title").text(),
			is("JAYCHAT!")
		);
	}
	
	@Test
	public void runAnotherTest() throws Exception {
		
		assertThat(
			jjTestRule.getAndWait("/index").select("script[data-jj-socket-url]").attr("data-jj-socket-url"),
			is(notNullValue())
			//is("ws://localhost/index/092f1fef937fabedcc3b4cd608e827a9e2bbc2b3.socket")
		);
	}
}
