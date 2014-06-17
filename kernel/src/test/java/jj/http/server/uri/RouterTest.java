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

import static org.junit.Assert.*;
import static io.netty.handler.codec.http.HttpMethod.*;

import java.util.ArrayList;
import java.util.List;

import jj.execution.MockTaskRunner;
import jj.http.server.uri.Route;
import jj.http.server.uri.Router;
import jj.http.server.uri.RouterConfiguration;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouterTest {
	
	MockTaskRunner mockTaskRunner = new MockTaskRunner();
	
	RouterConfiguration config = new RouterConfiguration() {
		
		@Override
		public String welcomeFile() {
			return "welcome";
		}
		
		@Override
		public List<Route> routes() {
			List<Route> result = new ArrayList<>();
			result.add(new Route(GET, "/", "/result1"));
			result.add(new Route(POST, "/", "/result1"));
			result.add(new Route(GET, "/chat/", "/result3"));
			result.add(new Route(POST, "/chat/:room", "/result4"));
			result.add(new Route(DELETE, "/chat/:room", "/result5"));
			result.add(new Route(GET, "/chat/:room", "/result6"));
			result.add(new Route(GET, "/chat/:room/*secret", "/result7"));
			
			return result;
		}
	};

	@Test
	public void test() throws Exception {
		
		Router router = new Router(config, mockTaskRunner);
		router.configurationLoaded(null);
		mockTaskRunner.runFirstTask();
	}

}
