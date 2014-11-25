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

import static io.netty.handler.codec.http.HttpMethod.*;

import java.util.Collections;
import java.util.Map;
import io.netty.handler.codec.http.HttpMethod;

/**
 * stores the router information in a trie for fast access
 * 
 * @author jason
 *
 */
class RouteTrie {
	
	private final TrieNode root = new SeparatorNode(TrieNode.PATH_SEPARATOR_CHAR);
	
	RouteTrie addRoute(Route route) {
		
		root.addRoute(route);
		return this;
	}
	
	RouteTrie compress() {
		root.compress();
		return this;
	}
	
	RouteMatch find(HttpMethod method, URIMatch uri) {
		assert method != null : "method is required";
		assert uri != null && !uri.uri.isEmpty() && uri.uri.charAt(0) == '/' : "uri is required and must start with /";
		
		// just play HEAD out as a GET
		if (HEAD.equals(method)) { method = GET; }
		
		RouteFinderContext context = new RouteFinderContext();
		RouteMatch result = null;
		
		if (root.findGoal(context, uri.path, 0)) {
			// 
			Map<HttpMethod, Route> routes = null;
			for (Route r : context.matches.get(0).goal.values()) {
				if (r.method().equals(method) || OPTIONS.equals(method)) {
					routes = context.matches.get(0).goal;
					break;
				}
			}
			
			@SuppressWarnings("unchecked")
			Map<String, String> params =
				(Map<String, String>)(context.matches.get(0).params == null ? Collections.emptyMap() : Collections.unmodifiableMap(context.matches.get(0).params));
			if (routes != null) {
				result = new RouteMatch(uri, method, routes, params);
			}
		}
		
		if (result == null) {
			result = new RouteMatch(uri, method, null, null);
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return root.toString();
	}
}
