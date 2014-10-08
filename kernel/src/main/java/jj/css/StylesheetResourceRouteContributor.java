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
package jj.css;

import io.netty.handler.codec.http.HttpMethod;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import jj.http.server.RouteContributor;
import jj.http.server.uri.Route;

/**
 * Sets up the mappings to stylesheet resources
 * @author jason
 *
 */
@Singleton
class StylesheetResourceRouteContributor implements RouteContributor {

	@Override
	public List<Route> contributions() {
		return Collections.singletonList(new Route(HttpMethod.GET, "/*path.css", "stylesheet", ""));
	}
}
