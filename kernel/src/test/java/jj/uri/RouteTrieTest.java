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
package jj.uri;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import io.netty.handler.codec.http.HttpMethod;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouteTrieTest {

	@Test
	public void test() {
		RouteTrie trie = new RouteTrie();
		
		trie.addRoute(HttpMethod.POST, "/this/is", "result 0");
		trie.addRoute(HttpMethod.GET, "/this/is/the/bomb", "result 1");
		trie.addRoute(HttpMethod.GET, "/this/is/the/best", "result 2");
		
		assertThat(trie.find(HttpMethod.POST, "/this/is"), is("result 0"));
		assertThat(trie.find(HttpMethod.GET, "/this/is"), is(nullValue()));
		assertThat(trie.find(HttpMethod.GET, "/this/is/the/bomb"), is("result 1"));
		assertThat(trie.find(HttpMethod.GET, "/this/is/the/best"), is("result 2"));
		assertThat(trie.find(HttpMethod.GET, "/this/is/the/bees-knees"), is(nullValue()));
	}
	
	@Test
	public void testDuplicateRoute() {
		
		RouteTrie trie = new RouteTrie();
		
		trie.addRoute(HttpMethod.POST, "/this/is", "result 0");
		
		try {
			trie.addRoute(HttpMethod.POST, "/this/is", "fail");
			fail();
		} catch (IllegalArgumentException iae) {
			assertThat(iae.getMessage(), is("duplicate route POST for /this/is"));
		}
		
	}

}
