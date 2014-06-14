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

import java.util.HashMap;
import java.util.Map;

import io.netty.handler.codec.http.HttpMethod;

/**
 * @author jason
 *
 */
class SeparatorNode<T> extends TrieNode<T> {
	
	Map<String, StringNode<T>> stringNodeChildren;
	int keyLength = 1;
	Map<String, ParamNode<T>> paramNodeChildren;
	

	@Override
	void doAddChild(HttpMethod method, String uri, T destination, int index) {
		
		char current = uri.charAt(index);
		
		assert current != SEPARATOR_CHAR : uri + " has two path separators in a row";
		
		if (PARAM_CHARS.indexOf(current) != -1) {
			paramNodeChildren = paramNodeChildren == null ? new HashMap<String, ParamNode<T>>(4) : paramNodeChildren;
			String paramValue = ParamNode.makeValue(uri, index);
			ParamNode<T> nextNode = paramNodeChildren.get(paramValue);
			if (nextNode == null) { 
				nextNode = new ParamNode<T>(paramValue);
				paramNodeChildren.put(paramValue, nextNode);
			}
			nextNode.addRoute(method, uri, destination, index + paramValue.length());
		} else {
			stringNodeChildren = stringNodeChildren == null ? new HashMap<String, StringNode<T>>(4) : stringNodeChildren;
			String value = String.valueOf(current);
			StringNode<T> nextNode = stringNodeChildren.get(value);
			if (nextNode == null) {
				nextNode = new StringNode<T>();
				stringNodeChildren.put(value, (StringNode<T>)nextNode);
			}
			nextNode.addRoute(method, uri, destination, index + 1);
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
		
		boolean result = false;
		
		if (stringNodeChildren != null && uri.length() >= index + keyLength) {
			String current = uri.substring(index, index + keyLength);
			if (stringNodeChildren.containsKey(current)) {
				result = stringNodeChildren.get(current).findGoal(context, uri, index + keyLength);
			}
		}
		
		if (paramNodeChildren != null) {
			for (ParamNode<T> paramNode : paramNodeChildren.values()) {
				boolean matched = paramNode.findGoal(context, uri, index);
				result = result || matched;
				
			}
		}
		
		return result;
	}
	
	@Override
	void compress() {
		if (stringNodeChildren != null) {
			if (stringNodeChildren.size() == 1) {
				String key = stringNodeChildren.keySet().iterator().next();
				StringBuilder accumulator = new StringBuilder(key);
				StringNode<T> node = stringNodeChildren.remove(key);
				StringNode<T> newNode = node.mergeUp(accumulator);
				keyLength = accumulator.length();
				newNode.compress();
				stringNodeChildren.put(accumulator.toString(), newNode);
			} else {
				for (StringNode<T> node : stringNodeChildren.values()) {
					node.compress();
				}
			}
		}
		
		if (paramNodeChildren != null) {
			for (ParamNode<T> node : paramNodeChildren.values()) {
				node.compress();
			}
		}
	}
	
	@Override
	void describeChildren(int indent, StringBuilder sb) {
		if (stringNodeChildren != null) {
			for (String key : stringNodeChildren.keySet()) {
				addIndentation(indent, sb.append("\n")).append(key).append(" = ");
				stringNodeChildren.get(key).describe(indent, sb);
			}
		}
		if (paramNodeChildren != null) {
			for (String key : paramNodeChildren.keySet()) {
				addIndentation(indent, sb.append("\n")).append(key).append(" = ");
				paramNodeChildren.get(key).describe(indent, sb);
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
