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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class StaticNode extends TrieNode {
	
	Map<String, TrieNode> children;
	int keyLength = 1;
	
	void doAddChild(Route route) {
		children = children == null ? new LinkedHashMap<String, TrieNode>(4, 0.75f) : children;
		TrieNode nextNode;
		char cur = route.currentChar();
		if (cur == PATH_SEPARATOR_CHAR ||
			(cur== EXTENSION_SEPARATOR_CHAR && !route.hasRemainingSegments())) {
			assert !terminal : "terminal node has additional trailing nodes!";
			
			String key = cur == PATH_SEPARATOR_CHAR ? PATH_SEPARATOR_STRING : EXTENSION_SEPARATOR_STRING;
			
			nextNode = children.get(key);
			if (nextNode == null) {
				nextNode = new SeparatorNode(cur);
				children.put(key, nextNode);
			}
		} else {
			String current = String.valueOf(cur);
			nextNode = children.get(current);
			if (nextNode == null) {
				nextNode = new StaticNode();
				children.put(current, nextNode);
			}
		}
		nextNode.addRoute(route.advanceIndex());
	}
	
	StaticNode mergeUp(StringBuilder accumulator) {
		if (children != null && children.size() == 1 && goal == null) {
			String key = children.keySet().iterator().next();
			if (children.get(key) instanceof StaticNode) {
				StaticNode node = (StaticNode)children.get(key);
				accumulator.append(key);
				return node.mergeUp(accumulator);
			}
		}
		
		return this;
	}
	
	@Override
	void doCompress() {
		if (children != null) {
			if (children.size() == 1) {
				String key = children.keySet().iterator().next();
				if (children.get(key) instanceof StaticNode) {
					StringBuilder accumulator = new StringBuilder(key);
					StaticNode node = (StaticNode)children.remove(key);
					TrieNode newNode = node.mergeUp(accumulator);
					keyLength = accumulator.length();
					newNode.compress();
					children = Collections.singletonMap(accumulator.toString(), newNode);
				} else {
					children = Collections.singletonMap(key, children.get(key));
					children.get(key).compress();
				}
			} else {
				for (TrieNode child : children.values()) {
					child.compress();
				}
				children = Collections.unmodifiableMap(children);
			}
		}
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
		
		if (children != null && uri.length() >= index + keyLength) {
			String current = uri.substring(index, index + keyLength);
			if (children.containsKey(current)) {
				return children.get(current).findGoal(context, uri, index + keyLength);
			}
		}
		
		return false;
	}
	
	@Override
	void describeChildren(int indent, StringBuilder sb) {
		if (children != null) {
			for (String key : children.keySet()) {
				addIndentation(indent, sb.append("\n")).append(key).append(" = ");
				children.get(key).describe(indent, sb);
			}
		}
	}
	
	@Override
	StringBuilder describe(int indent, StringBuilder sb) {
		sb.append(getClass().getSimpleName()).append(" - keylength: ").append(keyLength).append(", goal: ").append(goal);
		describeChildren(indent + 2, sb);
		return sb;
	}
}