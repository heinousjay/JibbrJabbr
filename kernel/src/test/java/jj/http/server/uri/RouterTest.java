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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static io.netty.handler.codec.http.HttpMethod.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jj.execution.MockTaskRunner;
import jj.http.server.RouteContributor;
import jj.http.server.uri.Route;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.Router;
import jj.http.server.uri.RouterConfiguration;
import jj.http.server.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouterTest {
	
	private static final String STATIC = "static";
	private static final String SOMETHING = "something";
	
	String welcome = "something.jpg";
	
	MockTaskRunner mockTaskRunner = new MockTaskRunner();
	
	RouterConfiguration config = new RouterConfiguration() {
		
		@Override
		public String welcomeFile() {
			return welcome;
		}
		
		@Override
		public List<Route> routes() {
			List<Route> result = new ArrayList<>();
			result.add(new Route(GET, "/start", STATIC, "/result1"));
			result.add(new Route(POST, "/finish", STATIC, "/result1"));
			result.add(new Route(GET, "/chat/", STATIC, "/result3"));
			result.add(new Route(POST, "/chat/:room", STATIC, "/result4"));
			result.add(new Route(DELETE, "/chat/:room", STATIC, "/result5"));
			result.add(new Route(GET, "/chat/:room", STATIC, "/result6"));
			result.add(new Route(GET, "/chat/:room/*secret", STATIC, "/result7"));
			
			return result;
		}
	};
	
	Set<RouteContributor> routeContributors;
	
	RouteContributor routeContributor1 = () -> Collections.singletonList(new Route(GET, "/*path.something", SOMETHING, ""));
	
	RouteContributor routeContributor2 = () -> {
		return Arrays.asList(
			new Route(POST, "/*path.something", SOMETHING, ""),
			new Route(DELETE, "/*path.something", SOMETHING, "")
		);
	};
	
	Router router;

	@Before
	public void before() throws Exception {
		
		routeContributors = new HashSet<>();
		routeContributors.add(routeContributor1);
		routeContributors.add(routeContributor2);
		
		router = new Router(config, routeContributors, mockTaskRunner);
		router.on(null);
		mockTaskRunner.runFirstTask();
	}
	
	
	@Test
	public void test() {

		RouteMatch routeMatch = router.routeRequest(GET, new URIMatch("/start"));
		
		assertThat(routeMatch.route.resourceName(), is(STATIC));
		assertThat(routeMatch.route.mapping(), is("/result1"));
		assertTrue(routeMatch.params.isEmpty());
		
		routeMatch = router.routeRequest(GET, new URIMatch("/something/../../../../../start"));
		
		assertThat(routeMatch.route.resourceName(), is(STATIC));
		assertThat(routeMatch.route.mapping(), is("/result1"));
		assertTrue(routeMatch.params.isEmpty());
		
		routeMatch = router.routeRequest(POST, new URIMatch("../finish"));
		
		assertThat(routeMatch.route.resourceName(), is(STATIC));
		assertThat(routeMatch.route.mapping(), is("/result1"));
		assertTrue(routeMatch.params.isEmpty());
		
		routeMatch = router.routeRequest(GET, new URIMatch("/some/path/to.something"));
		
		assertThat(routeMatch.route.resourceName(), is(SOMETHING));
		assertThat(routeMatch.route.mapping(), is(""));
		assertThat(routeMatch.params.size(), is(1));
		assertThat(routeMatch.params.get("path"), is("some/path/to"));
		
		routeMatch = router.routeRequest(POST, new URIMatch("/some/path/to.something"));
		
		assertThat(routeMatch.route.resourceName(), is(SOMETHING));
		assertThat(routeMatch.route.mapping(), is(""));
		assertThat(routeMatch.params.size(), is(1));
		assertThat(routeMatch.params.get("path"), is("some/path/to"));
		
		routeMatch = router.routeRequest(DELETE, new URIMatch("/some/path/to.something"));
		
		assertThat(routeMatch.route.resourceName(), is(SOMETHING));
		assertThat(routeMatch.route.mapping(), is(""));
		assertThat(routeMatch.params.size(), is(1));
		assertThat(routeMatch.params.get("path"), is("some/path/to"));
		
		
//		assertThat(router.find("/index"), is("/index"));
//		assertThat(router.find("/other"), is("/other"));
//		assertThat(router.find("/other/"), is("/other/index"));
//		assertThat(router.find("/other/index"), is("/other/index"));
//		assertThat(router.find("/other/other"), is("/other/other"));
//		assertThat(router.find("../other/"), is("/other/index"));
//		assertThat(router.find("../other/index"), is("/other/index"));
//		assertThat(router.find("../other/other"), is("/other/other"));
//		assertThat(router.find("/../../../other/"), is("/other/index"));
//		assertThat(router.find("/../../../other/index"), is("/other/index"));
//		assertThat(router.find("/../../../other/other"), is("/other/other"));
	}

}
