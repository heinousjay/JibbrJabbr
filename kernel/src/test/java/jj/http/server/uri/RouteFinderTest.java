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
import static org.hamcrest.Matchers.*;
import jj.http.server.uri.RouteFinder;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouteFinderTest {

	@Test
	public void testDefaultRouting() {
		RouteFinder routeFinder = new RouteFinder();
		
		assertThat(routeFinder.find("/"), is("/index"));
		assertThat(routeFinder.find("/index"), is("/index"));
		assertThat(routeFinder.find("/other"), is("/other"));
		assertThat(routeFinder.find("/other/"), is("/other/index"));
		assertThat(routeFinder.find("/other/index"), is("/other/index"));
		assertThat(routeFinder.find("/other/other"), is("/other/other"));
		assertThat(routeFinder.find("../other/"), is("/other/index"));
		assertThat(routeFinder.find("../other/index"), is("/other/index"));
		assertThat(routeFinder.find("../other/other"), is("/other/other"));
		assertThat(routeFinder.find("/../../../other/"), is("/other/index"));
		assertThat(routeFinder.find("/../../../other/index"), is("/other/index"));
		assertThat(routeFinder.find("/../../../other/other"), is("/other/other"));
	}

}
