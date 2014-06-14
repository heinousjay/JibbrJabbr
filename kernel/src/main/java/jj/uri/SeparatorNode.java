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
class SeparatorNode extends TrieNode {
	
	Map<String, StringNode> stringNodeChildren;
	Map<String, ParamNode> paramNodeChildren;
	

	@Override
	void doAddChild(HttpMethod method, String uri, String destination, int index) {
		
		char current = uri.charAt(index);
		
		assert current != SEPARATOR_CHAR : uri + " has two path separators in a row";
		
		if (PARAM_CHARS.indexOf(current) != -1) {
			paramNodeChildren = paramNodeChildren == null ? new HashMap<String, ParamNode>(4) : paramNodeChildren;
			String paramValue = ParamNode.makeValue(uri, index);
			ParamNode nextNode = paramNodeChildren.get(paramValue);
			if (nextNode == null) { 
				nextNode = new ParamNode(paramValue);
				paramNodeChildren.put(paramValue, nextNode);
			}
			nextNode.addRoute(method, uri, destination, index + paramValue.length());
		} else {
			stringNodeChildren = stringNodeChildren == null ? new HashMap<String, StringNode>(4) : stringNodeChildren;
			String value = String.valueOf(current);
			StringNode nextNode = stringNodeChildren.get(value);
			if (nextNode == null) {
				nextNode = new StringNode();
				stringNodeChildren.put(value, (StringNode)nextNode);
			}
			nextNode.addRoute(method, uri, destination, index + 1);
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
		
		if (stringNodeChildren != null) {
			String current = uri.substring(index, index + 1);
			if (stringNodeChildren.containsKey(current)) {
				result = stringNodeChildren.get(current).findGoal(context, uri, index + 1);
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
	public String toString() {
		return getClass().getSimpleName() + "(goal: " + goal + "\nstringNodeChildren: " + stringNodeChildren + "\ndynamicChildren: " + paramNodeChildren + ")";
	}
}
