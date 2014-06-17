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
package jj.uri;

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
	
	private final TrieNode root = new SeparatorNode();
	
	void addRoute(Route route) {
		
		root.addRoute(route);
	}
	
	RouteTrie compress() {
		root.compress();
		return this;
	}
	
	RouteMatch find(HttpMethod method, String uri) {
		assert method != null : "method is required";
		assert uri != null && !uri.isEmpty() && uri.charAt(0) == '/' : "uri is required and must start with /";
		
		RouteFinderContext context = new RouteFinderContext();
		RouteMatch result = null;
		if (root.findGoal(context, uri, 1)) {
			// well just use the first one here
			Route route = null;
			for (Route r : context.matches.get(0).goal) {
				if (r.method().equals(method)) {
					route = r;
					break;
				}
			}
			
			@SuppressWarnings("unchecked")
			Map<String, String> params =
				(Map<String, String>)(context.matches.get(0).params == null ? Collections.emptyMap() : Collections.unmodifiableMap(context.matches.get(0).params));
			if (route != null) {
				result = new RouteMatch(route, params);
			}
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return root.toString();
	}
}
