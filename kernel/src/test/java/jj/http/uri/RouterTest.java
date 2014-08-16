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
package jj.http.uri;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static io.netty.handler.codec.http.HttpMethod.*;

import java.util.ArrayList;
import java.util.List;

import jj.execution.MockTaskRunner;
import jj.http.uri.Route;
import jj.http.uri.RouteMatch;
import jj.http.uri.Router;
import jj.http.uri.RouterConfiguration;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouterTest {
	
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
			result.add(new Route(GET, "/start", "/result1"));
			result.add(new Route(POST, "/finish", "/result1"));
			result.add(new Route(GET, "/chat/", "/result3"));
			result.add(new Route(POST, "/chat/:room", "/result4"));
			result.add(new Route(DELETE, "/chat/:room", "/result5"));
			result.add(new Route(GET, "/chat/:room", "/result6"));
			result.add(new Route(GET, "/chat/:room/*secret", "/result7"));
			
			return result;
		}
	};
	
	Router router;

	@Before
	public void before() throws Exception {
		
		router = new Router(config, mockTaskRunner);
		router.configurationLoaded(null);
		mockTaskRunner.runFirstTask();
	}
	
	
	@Test
	public void test() {

		RouteMatch routeMatch = router.matchURI(GET, new URIMatch("/"));
		
		assertThat(routeMatch.route.destination(), is("/" + welcome));
		assertTrue(routeMatch.params.isEmpty());
		
		routeMatch = router.matchURI(GET, new URIMatch("/something/../../"));
		
		assertThat(routeMatch.route.destination(), is("/" + welcome));
		assertTrue(routeMatch.params.isEmpty());
		
		routeMatch = router.matchURI(GET, new URIMatch("../"));
		
		assertThat(routeMatch.route.destination(), is("/" + welcome));
		assertTrue(routeMatch.params.isEmpty());
		
		
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
