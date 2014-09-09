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
import java.util.Map.Entry;

/**
 * @author jason
 *
 */
class SeparatorNode extends TrieNode {
	
	Map<String, StaticNode> stringNodeChildren;
	int keyLength = 1;
	Map<String, ParamNode> paramNodeChildren;
	
	final char separator;
	
	SeparatorNode(final char separator) {
		this.separator = separator;
	}
	

	@Override
	void doAddChild(Route route) {
		
		char current = route.currentChar();
		
		// not correctly an assertion! should throw IllegalArgumentException?
		// need to write a bunch of tests for illegal routes to validate this stuff
		assert current != PATH_SEPARATOR_CHAR : route + " has two separators in a row";
		
		if (PARAM_CHARS.indexOf(current) != -1) {
			paramNodeChildren = paramNodeChildren == null ? new LinkedHashMap<String, ParamNode>(4) : paramNodeChildren;
			String paramValue = ParamNode.makeValue(route);
			ParamNode nextNode = paramNodeChildren.get(paramValue);
			if (nextNode == null) { 
				nextNode = new ParamNode(route);
				nextNode.terminal = terminal;
				paramNodeChildren.put(paramValue, nextNode);
			} else {
				route.addParam(nextNode.parameter);
			}
			nextNode.addRoute(route.advanceIndex(paramValue.length()));
		} else {
			stringNodeChildren = stringNodeChildren == null ? new LinkedHashMap<String, StaticNode>(4) : stringNodeChildren;
			String value = String.valueOf(current);
			StaticNode nextNode = stringNodeChildren.get(value);
			if (nextNode == null) {
				nextNode = new StaticNode();
				nextNode.terminal = terminal;
				stringNodeChildren.put(value, (StaticNode)nextNode);
			}
			nextNode.addRoute(route.advanceIndex());
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
		
		boolean result = false;
		
		if (stringNodeChildren != null && uri.length() >= index + keyLength) {
			String current = uri.substring(index, index + keyLength);
			if (stringNodeChildren.containsKey(current)) {
				boolean matched = stringNodeChildren.get(current).findGoal(context, uri, index + keyLength);
				result = result || matched;
			}
		}
		
		if (paramNodeChildren != null) {
			for (ParamNode paramNode : paramNodeChildren.values()) {
				boolean matched = paramNode.findGoal(context, uri, index);
				result = result || matched;
				
			}
		}
		
		return result;
	}
	
	@Override
	void doCompress() {
		if (stringNodeChildren != null) {
			if (stringNodeChildren.size() == 1) {
				String key = stringNodeChildren.keySet().iterator().next();
				StringBuilder accumulator = new StringBuilder(key);
				StaticNode node = stringNodeChildren.remove(key);
				StaticNode newNode = node.mergeUp(accumulator);
				keyLength = accumulator.length();
				newNode.compress();
				stringNodeChildren = Collections.singletonMap(accumulator.toString(), newNode);
			} else {
				for (StaticNode node : stringNodeChildren.values()) {
					node.compress();
				}
				stringNodeChildren = Collections.unmodifiableMap(stringNodeChildren);
			}
		}
		
		if (paramNodeChildren != null) {
			for (ParamNode node : paramNodeChildren.values()) {
				node.compress();
			}
			// silly optimization
			if (paramNodeChildren.size() == 1) {
				Entry<String, ParamNode> entry = paramNodeChildren.entrySet().iterator().next();
				paramNodeChildren = Collections.singletonMap(entry.getKey(), entry.getValue());
			} else {
				paramNodeChildren = Collections.unmodifiableMap(paramNodeChildren);
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
