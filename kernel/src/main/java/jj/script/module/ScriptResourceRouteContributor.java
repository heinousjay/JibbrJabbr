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
package jj.script.module;

import static io.netty.handler.codec.http.HttpMethod.GET;

import java.util.Collections;
import java.util.List;

import jj.http.server.RouteContributor;
import jj.http.server.uri.Route;

/**
 * @author jason
 *
 */
class ScriptResourceRouteContributor implements RouteContributor {

	@Override
	public List<Route> contributions() {
		return Collections.singletonList(new Route(GET, "/*path.js", "script", ""));
	}

}
