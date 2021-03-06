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

import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

public class RouteMatch {

	final URIMatch uriMatch;
	final Route route;
	public final Map<HttpMethod, Route> routes;
	final Map<String, String> params;

	RouteMatch(final URIMatch uriMatch, final HttpMethod method, final Map<HttpMethod, Route> routes, final Map<String, String> params) {
		this.uriMatch = uriMatch;
		this.route = routes == null ? null : routes.get(method);
		this.routes = routes;
		this.params = params;
	}

	public boolean matched() {
		return route != null;
	}

	public Route route() {
		return route;
	}

	/**
	 * @return the resource name of the matched route, if any
	 */
	public String resourceName() {
		return route == null ? null : route.resourceName();
	}

	public String toString() {
		return String.format("RouteMatch(%n  uriMatch=%s%n  route=%s%n  routes=%s%n  params=%s%n)", uriMatch, route, routes, params);
	}
}