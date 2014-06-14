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

class StringNode extends TrieNode {
	
	Map<String, TrieNode> children;
	
	void doAddChild(HttpMethod method, String uri, String destination, int index) {
		children = children == null ? new HashMap<String, TrieNode>(4, 0.75f) : children;
		TrieNode nextNode;
		if (uri.charAt(index) == SEPARATOR_CHAR) {
			nextNode = children.get(SEPARATOR_STRING);
			if (nextNode == null) {
				nextNode = new SeparatorNode();
				children.put(SEPARATOR_STRING, nextNode);
			}
			
			
		} else {

			String current = uri.substring(index, index + 1);
			nextNode = children.get(current);
			if (nextNode == null) {
				nextNode = new StringNode();
				children.put(current, nextNode);
			}
		}
		nextNode.addRoute(method, uri, destination, index + 1);
	}
	
	@Override
	boolean findGoal(RouteFinderContext context, String uri, int index) {
		if (uri.length() == index) {
			if (goal != null) {
				context.setGoal(goal);
				return true;
			}
			return false;
		}
		
		if (children != null) {
			String current = uri.substring(index, index + 1);
			if (children.containsKey(current)) {
				return children.get(current).findGoal(context, uri, index + 1);
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(goal: " + goal + ", children: " + children + ")";
	}
}