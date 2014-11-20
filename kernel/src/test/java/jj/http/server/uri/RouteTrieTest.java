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
import jj.http.server.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouteTrieTest {
	
	private static final String DOCUMENT = "document";
	private static final String SCRIPT = "script";
	private static final String STYLESHEET = "stylesheet";
	private static final String STATIC   = "static";
	
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
	
	private RouteTrie makeMixedUpTrie() {
		return new RouteTrie()
			.addRoute(new Route(GET, "/d3", DOCUMENT, "d3/index"))
			.addRoute(new Route(GET, "/chat", DOCUMENT, "chat/index"))
			.addRoute(new Route(GET, "/*path.css", STYLESHEET, ""))
			.addRoute(new Route(GET, "/*path.js", SCRIPT, ""))
			//.addRoute(new Route(GET, "/*path.:ext", STATIC, ""))
			.addRoute(new Route(GET, "/*fallthrough", STATIC, ""));
	}
	
	RouteMatch result;
	
	@Before
	public void before() {
		result = null;
	}
	
	@Test
	public void testMixedUp() {
		RouteTrie rt = makeMixedUpTrie().compress();
		
		System.out.println(rt);
		
		result = rt.find(GET, new URIMatch("/d3/"));
		assertResult(DOCUMENT, "d3/index");
		
		result = rt.find(GET, new URIMatch("/d3"));
		assertResult(DOCUMENT, "d3/index");
		
		result = rt.find(GET, new URIMatch("/chat/"));
		assertResult(DOCUMENT, "chat/index");
		
		result = rt.find(GET, new URIMatch("/chat"));
		assertResult(DOCUMENT, "chat/index");
		
		result = rt.find(GET, new URIMatch("/chat.html"));
		assertResult(STATIC, "");
		
		result = rt.find(GET, new URIMatch("/chat/index.html"));
		assertResult(STATIC, "");
		
		result = rt.find(GET, new URIMatch("/chat/index.js"));
		assertResult(SCRIPT, "");
		
		result = rt.find(GET, new URIMatch("/something/else.html"));
		assertResult(STATIC, "");
		
		result = rt.find(GET, new URIMatch("/something/else.css"));
		assertResult(STYLESHEET, "");
		
		result = rt.find(GET, new URIMatch("/something/else.js"));
		assertResult(SCRIPT, "");
		
		result = rt.find(GET, new URIMatch("/something/else"));
		assertResult(STATIC, "");
	}
	
	private void assertResult(String resourceName, String mapping) {
		assertTrue(result.matched());
		assertThat(result.resourceName(), is(resourceName));
		assertThat(result.route.mapping(), is(mapping));
	}
	
	private RouteTrie makeParameterMatchTrie() {
		
		return new RouteTrie()
		// admittedly not the best example
			.addRoute(new Route(GET, "/user/:id([a-z]-[\\d]{6})/picture", DOCUMENT, result(0)))
			.addRoute(new Route(GET, "/user/:name([\\w]+)/picture",       DOCUMENT, result(1)))
			.addRoute(new Route(GET, "/this/:is/:the/best",               DOCUMENT, result(2)))
			.addRoute(new Route(GET, "/this/:is/:the/*end",               DOCUMENT, result(3)));
	}
	
	@Test
	public void testParameterMatching() {
		RouteTrie trie = makeParameterMatchTrie().compress();
		
		RouteMatch result = trie.find(GET, new URIMatch("/user/a-123456/picture"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(0)));
		assertThat(result.params.get("id"), is("a-123456"));
		assertThat(result.params.size(), is(1));
		
		assertThat(result.route.resolve(makeMap("id", "b-987654")), is("/user/b-987654/picture"));
		assertThat(result.route.resolve(makeMap("id", "c-098765")), is("/user/c-098765/picture"));
		
		result = trie.find(GET, new URIMatch("/user/jason/picture"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(1)));
		assertThat(result.params.get("name"), is("jason"));
		assertThat(result.params.size(), is(1));
		
		assertThat(result.route.resolve(makeMap("name", "jason")), is("/user/jason/picture"));
		assertThat(result.route.resolve(makeMap("name", "test")), is("/user/test/picture"));
		
		result = trie.find(GET, new URIMatch("/this/time/it/is/personal"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(3)));
		assertThat(result.params.get("is"), is("time"));
		assertThat(result.params.get("the"), is("it"));
		assertThat(result.params.get("end"), is("is/personal"));
		assertThat(result.params.size(), is(3));
		
		result = trie.find(GET, new URIMatch("/this/is/not-the/best"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(2)));
		assertThat(result.params.get("is"), is("is"));
		assertThat(result.params.get("the"), is("not-the"));
		assertThat(result.params.size(), is(2));
	}
	
	private RouteTrie makeRouteTrie() {
		return new RouteTrie()
		
			.addRoute(new Route(POST,   "/this/is",                     STATIC, result(0)))
			.addRoute(new Route(DELETE, "/this/isno",                   STATIC, result(-100)))
			.addRoute(new Route(PUT,    "/this/isno",                   STATIC, result(-200)))
			.addRoute(new Route(GET,    "/this/isno",                   STATIC, result(-300)))
			.addRoute(new Route(POST,   "/this/isno",                   STATIC, result(-400)))
			.addRoute(new Route(GET,    "/this/isnot",                  STATIC, result(-1)))
			.addRoute(new Route(GET,    "/this/is/the/bomb",            STATIC, result(1)))
			.addRoute(new Route(GET,    "/this/is/the/bomberman",       STATIC, result(1000)))
			.addRoute(new Route(GET,    "/this/is/the/best",            STATIC, result(2)))
			.addRoute(new Route(GET,    "/this/is/the/best-around",     STATIC, result(2000)))
			.addRoute(new Route(GET,    "/this/is/the.dir/bomb",        STATIC, result(1001)))
			.addRoute(new Route(GET,    "/this/is/the.dir/bomberman",   STATIC, result(1002)))
			.addRoute(new Route(GET,    "/this/is/the.dir/best",        STATIC, result(2001)))
			.addRoute(new Route(GET,    "/this/is/the.dir/best-around", STATIC, result(2002)))
			.addRoute(new Route(GET,    "/this/:is/:the/best",          STATIC, result(3)))
			.addRoute(new Route(GET,    "/this/:is/:the/*end",          STATIC, result(4)))
		
		// just to validate the structure works in order,
		// these rules would basically eat up everything but since they're at
		// the end, they match but don't get involved
			.addRoute(new Route(GET,    "/this/*islast_and_should_not_interfere", STATIC, result(4000)))
			.addRoute(new Route(GET,    "/this/*islast_and_also_is_not_used",     STATIC, result(5000)))
		
		// the idea here is that this should pick up ANYTHING that ends in .css and wasn't picked up by
		// anything before this.  which implies that matching needs to ignore extensions generally?
			.addRoute(new Route(GET, "/some.directory/static.:ext", DOCUMENT, result(5998)))
			.addRoute(new Route(GET, "/some.directory/*path.css", DOCUMENT, result(5999)))
			.addRoute(new Route(GET, "/*path.css",  DOCUMENT, result(6000)))
			.addRoute(new Route(GET, "/*path.:ext", STATIC, result(7000)));
	}
	
	private void testRouteTrie(RouteTrie trie) {

		RouteMatch result = trie.find(POST, new URIMatch("/this/is"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(0)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(DELETE, new URIMatch("/this/isno"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(-100)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, new URIMatch("/this/isnot"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(-1)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, new URIMatch("/this/is"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route, is(nullValue()));
		assertThat(result.routes, is(nullValue()));
		assertThat(result.params, is(nullValue()));
		
		result = trie.find(GET, new URIMatch("/this/is/the/bomb"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(1)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, new URIMatch("/this/is/the.dir/bomb"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(1001)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, new URIMatch("/this/is/the/best"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(2)));
		assertTrue(result.params.isEmpty());
		
		result = trie.find(GET, new URIMatch("/this/works/the/best"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(3)));
		assertThat(result.params.get("is"), is("works"));
		assertThat(result.params.get("the"), is("the"));
		assertThat(result.params.size(), is(2));
		
		result = trie.find(GET, new URIMatch("/this/makes/me/bees-knees/if-you-know/what-i-am-saying"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(4)));
		assertThat(result.params.get("is"), is("makes"));
		assertThat(result.params.get("the"), is("me"));
		assertThat(result.params.get("end"), is("bees-knees/if-you-know/what-i-am-saying"));
		assertThat(result.params.size(), is(3));
		
		result = trie.find(GET, new URIMatch("/jason.css"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(6000)));
		assertThat(result.params.get("path"), is("jason"));
		assertThat(result.params.size(), is(1));
		
		result = trie.find(GET, new URIMatch("/jason"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route, is(nullValue()));
		assertThat(result.routes, is(nullValue()));
		assertThat(result.params, is(nullValue()));
		
		result = trie.find(GET, new URIMatch("/jason/made/this/work.css"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(6000)));
		assertThat(result.params.get("path"), is("jason/made/this/work"));
		assertThat(result.params.size(), is(1));
		
		result = trie.find(GET, new URIMatch("/jason/made/this/work"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route, is(nullValue()));
		assertThat(result.routes, is(nullValue()));
		assertThat(result.params, is(nullValue()));
		
		result = trie.find(GET, new URIMatch("/jason/made/this/work.cs1"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(STATIC));
		assertThat(result.route.mapping(), is(result(7000)));
		assertThat(result.params.get("path"), is("jason/made/this/work"));
		assertThat(result.params.get("ext"), is("cs1"));
		assertThat(result.params.size(), is(2));
		
		result = trie.find(GET, new URIMatch("/some.directory/file.css"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(5999)));
		assertThat(result.params.get("path"), is("file"));
		assertThat(result.params.size(), is(1));
		
		result = trie.find(GET, new URIMatch("/some.directory/static.css"));
		assertThat(result, is(notNullValue()));
		assertThat(result.route.resourceName(), is(DOCUMENT));
		assertThat(result.route.mapping(), is(result(5998)));
		assertThat(result.params.get("ext"), is("css"));
		assertThat(result.params.size(), is(1));
		
		
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
		
		RouteMatch routeMatch = trie.find(OPTIONS, new URIMatch("/this/isno"));
		
		assertThat(routeMatch.routes, is(notNullValue()));
		assertThat(routeMatch.routes.keySet(), containsInAnyOrder(GET, POST, PUT, DELETE));
		assertThat(routeMatch.route, is(nullValue()));
		
		routeMatch = trie.find(OPTIONS, new URIMatch("/this/is/the/best"));
		
		assertThat(routeMatch.routes, is(notNullValue()));
		assertThat(routeMatch.routes.keySet(), contains(GET));
		assertThat(routeMatch.route, is(nullValue()));
		
		routeMatch = trie.find(HEAD, new URIMatch("/this/isno"));
		
		assertThat(routeMatch.routes, is(notNullValue()));
		assertThat(routeMatch.route.method(), is(GET));
		assertThat(routeMatch.route.mapping(), is(result(-300)));
	}
	
	@Test
	public void testDuplicateRoute() {
		
		RouteTrie trie = new RouteTrie()
			.addRoute(new Route(POST, "/this/is", STATIC, "/success"));
		
		try {
			trie.addRoute(new Route(POST, "/this/is", STATIC, "/failure"));
			fail();
		} catch (IllegalArgumentException iae) {
			assertThat(
				iae.getMessage(), 
				is("duplicate route POST /this/is to static mapped as '/failure' with params null, current config = {POST=route POST /this/is to static mapped as '/success' with params null}")
			);
		}
	}
}
