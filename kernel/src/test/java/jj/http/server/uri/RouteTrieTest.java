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
package jj.http.server.uri;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static io.netty.handler.codec.http.HttpMethod.*;

import java.util.HashMap;
import java.util.Map;

import jj.http.server.uri.Route;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.RouteTrie;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouteTrieTest {
	
	private String result(int index) {
		return "/result" + index;
	}
	
	private Map<String, String> makeMap(String...s) {
		assert s.length % 2 == 0;
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < s.length; i += 2) {
			map.put(s[i], s[i + 1]);
		}
		return map;
	}
	
	private RouteTrie makeParameterMatchTrie() {
		
		RouteTrie trie = new RouteTrie();
		// admittedly not the best example
		trie.addRoute(new Route(GET, "/user/:id([a-z]-[\\d]{6})/picture", result(0)));
		trie.addRoute(new Route(GET, "/user/:name([\\w]+)/picture",       result(1)));
		trie.addRoute(new Route(GET, "/this/:is/:the/best",               result(2)));
		trie.addRoute(new Route(GET, "/this/:is/:the/*end",               result(3)));
		
		return trie;
	}
	
	@Test
	public void testParameterMatching() {
		RouteTrie trie = makeParameterMatchTrie().compress();
		
		RouteMatch result = trie.find(GET, "/user/a-123456/picture");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(0)));
		assertThat(result.params.get("id"), is("a-123456"));
		assertThat(result.params.size(), is(1));
		
		assertThat(result.route.resolve(makeMap("id", "b-987654")), is("/user/b-987654/picture"));
		assertThat(result.route.resolve(makeMap("id", "c-098765")), is("/user/c-098765/picture"));
		
		result = trie.find(GET, "/user/jason/picture");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(1)));
		assertThat(result.params.get("name"), is("jason"));
		assertThat(result.params.size(), is(1));
		
		assertThat(result.route.resolve(makeMap("name", "jason")), is("/user/jason/picture"));
		assertThat(result.route.resolve(makeMap("name", "test")), is("/user/test/picture"));
		
		result = trie.find(GET, "/this/time/it/is/personal");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(3)));
		assertThat(result.params.get("is"), is("time"));
		assertThat(result.params.get("the"), is("it"));
		assertThat(result.params.get("end"), is("is/personal"));
		assertThat(result.params.size(), is(3));
		
		result = trie.find(GET, "/this/is/not-the/best");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(2)));
		assertThat(result.params.get("is"), is("is"));
		assertThat(result.params.get("the"), is("not-the"));
		assertThat(result.params.size(), is(2));
	}
	
	private RouteTrie makeRouteTrie() {
		RouteTrie trie = new RouteTrie();
		
		trie.addRoute(new Route(POST,   "/this/is",                 result(0)));
		trie.addRoute(new Route(DELETE, "/this/isno",               result(-100)));
		trie.addRoute(new Route(PUT,    "/this/isno",               result(-200)));
		trie.addRoute(new Route(GET,    "/this/isno",               result(-300)));
		trie.addRoute(new Route(POST,   "/this/isno",               result(-400)));
		trie.addRoute(new Route(GET,    "/this/isnot",              result(-1)));
		trie.addRoute(new Route(GET,    "/this/is/the/bomb",        result(1)));
		trie.addRoute(new Route(GET,    "/this/is/the/bomberman",   result(1000)));
		trie.addRoute(new Route(GET,    "/this/is/the/best",        result(2)));
		trie.addRoute(new Route(GET,    "/this/is/the/best-around", result(2000)));
		trie.addRoute(new Route(GET,    "/this/:is/:the/best",      result(3)));
		trie.addRoute(new Route(GET,    "/this/:is/:the/*end",      result(4)));
		
		// just to validate the structure works in order,
		// these rules would basically eat up everything but since they're at
		// the end, they match but don't get involved
		trie.addRoute(new Route(GET,    "/this/*islast-and-should-not-interfere", result(4000)));
		trie.addRoute(new Route(GET,    "/this/*islast-and-also-is-not-used",     result(5000)));
		
		return trie;
	}
	
	private void testRouteTrie(RouteTrie trie) {

		RouteMatch result = trie.find(POST, "/this/is");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(0)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(DELETE, "/this/isno");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(-100)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, "/this/isnot");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(-1)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, "/this/is");
		assertThat(result, is(nullValue()));
		
		result = trie.find(GET, "/this/is/the/bomb");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(1)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, "/this/is/the/best");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(2)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, "/this/works/the/best");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(3)));
		assertThat(result.params.get("is"), is("works"));
		assertThat(result.params.get("the"), is("the"));
		assertThat(result.params.size(), is(2));
		
		result = trie.find(GET, "/this/makes/me/bees-knees/if-you-know/what-i-am-saying");
		assertThat(result, is(notNullValue()));
		assertThat(result.route.destination(), is(result(4)));
		assertThat(result.params.get("is"), is("makes"));
		assertThat(result.params.get("the"), is("me"));
		assertThat(result.params.get("end"), is("bees-knees/if-you-know/what-i-am-saying"));
		assertThat(result.params.size(), is(3));
	}
	
	@Test
	public void testUncompressed() {
		testRouteTrie(makeRouteTrie());
	}
	
	@Test
	public void testCompressed() {
		RouteTrie trie = makeRouteTrie();

		//System.out.println(trie);
		
		trie.compress();
		
		//System.out.println(trie);
		
		testRouteTrie(trie);
	}
	
	@Test
	public void testSpecialMethodHandling() {
		RouteTrie trie = makeRouteTrie().compress();
		
		//System.out.println(trie);
		
		RouteMatch routeMatch = trie.find(OPTIONS, "/this/isno");
		
		assertThat(routeMatch.routes, is(notNullValue()));
		assertThat(routeMatch.routes.keySet(), containsInAnyOrder(GET, POST, PUT, DELETE));
		assertThat(routeMatch.route, is(nullValue()));
		
		routeMatch = trie.find(OPTIONS, "/this/is/the/best");
		
		assertThat(routeMatch.routes, is(notNullValue()));
		assertThat(routeMatch.routes.keySet(), contains(GET));
		assertThat(routeMatch.route, is(nullValue()));
		
		routeMatch = trie.find(HEAD, "/this/isno");
		
		assertThat(routeMatch.routes, is(notNullValue()));
		assertThat(routeMatch.route.method(), is(GET));
		assertThat(routeMatch.route.destination(), is(result(-300)));
	}
	
	@Test
	public void testDuplicateRoute() {
		
		RouteTrie trie = new RouteTrie();
		
		trie.addRoute(new Route(POST, "/this/is", "/success"));
		
		try {
			trie.addRoute(new Route(POST, "/this/is", "/failure"));
			fail();
		} catch (IllegalArgumentException iae) {
			assertThat(
				iae.getMessage(), 
				is("duplicate route POST /this/is to /failure with params null, current config = {POST=route POST /this/is to /success with params null}")
			);
		}
	}
}