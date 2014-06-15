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

import java.util.LinkedHashMap;
import java.util.Map;

class StringNode<T> extends TrieNode<T> {
	
	Map<String, TrieNode<T>> children;
	int keyLength = 1;
	
	void doAddChild(HttpMethod method, String uri, T destination, int index) {
		children = children == null ? new LinkedHashMap<String, TrieNode<T>>(4, 0.75f) : children;
		TrieNode<T> nextNode;
		if (uri.charAt(index) == SEPARATOR_CHAR) {
			nextNode = children.get(SEPARATOR_STRING);
			if (nextNode == null) {
				nextNode = new SeparatorNode<T>();
				children.put(SEPARATOR_STRING, nextNode);
			}
			
			
		} else {

			String current = uri.substring(index, index + 1);
			nextNode = children.get(current);
			if (nextNode == null) {
				nextNode = new StringNode<T>();
				children.put(current, nextNode);
			}
		}
		nextNode.addRoute(method, uri, destination, index + 1);
	}
	
	StringNode<T> mergeUp(StringBuilder accumulator) {
		if (children != null && children.size() == 1) {
			String key = children.keySet().iterator().next();
			if (!SEPARATOR_STRING.equals(key)) {
				StringNode<T> node = (StringNode<T>)children.get(key);
				if (node.goal == null) {
					accumulator.append(key);
					return node.mergeUp(accumulator);
				}
			}
		}
		
		return this;
	}
	
	@Override
	void compress() {
		if (children != null) {
			if (children.size() == 1) {
				String key = children.keySet().iterator().next();
				StringBuilder accumulator = new StringBuilder(key);
				if (!SEPARATOR_STRING.equals(key)) {
					StringNode<T> node = (StringNode<T>)children.remove(key);
					StringNode<T> newNode = node.mergeUp(accumulator);
					keyLength = accumulator.length();
					newNode.compress();
					children.put(accumulator.toString(), newNode);
				} else {
					children.get(key).compress();
				}
			} else {
				for (TrieNode<T> child : children.values()) {
					child.compress();
				}
			}
		}
	}
	
	@Override
	boolean findGoal(RouteFinderContext<T> context, String uri, int index) {
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