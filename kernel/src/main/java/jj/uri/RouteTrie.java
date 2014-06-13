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

import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * stores the router information in a trie for fast access
 * 
 * @author jason
 *
 */
class RouteTrie {

	class Node {
		
		Map<HttpMethod, String> goal;
		Map<String, Node> children;
		
		
		void addRoute(HttpMethod method, String uri, String destination, int index) {
			if (uri.length() == index) {
				goal = goal == null ? new HashMap<HttpMethod, String>(2, 0.5f) : goal;
				if (goal.containsKey(method)) {
					throw new IllegalArgumentException("duplicate route " + method + " for " + uri);
				}
				goal.put(method, destination);
			} else {
				children = children == null ? new HashMap<String, Node>(4, 0.75f) : children;
				
				String current = uri.substring(index, index + 1);
				
				Node nextNode = children.get(current);
				if (nextNode == null) {
					nextNode = new Node();
					children.put(current, nextNode);
				}
				
				nextNode.addRoute(method, uri, destination, index + 1);
			}
		}
		
		String find(HttpMethod method, String uri, int index) {
			if (uri.length() == index) {
				return goal != null ? goal.get(method) : null;
			}
			
			if (children != null) {
				String current = uri.substring(index, index + 1);
				if (children.containsKey(current)) {
					return children.get(current).find(method, uri, index + 1);
				}
			}
			
			return null;
		}
		
		@Override
		public String toString() {
			return "goal: " + goal + "\nchildren: " + children;
		}
	}
	
	private final Node root = new Node();
	
	void addRoute(HttpMethod method, String uri, String destination) {
		root.addRoute(method, uri, destination, 0);
	}
	
	String find(HttpMethod method, String uri) {
		return root.find(method, uri, 0);
	}
	
	@Override
	public String toString() {
		return root.toString();
	}
}
