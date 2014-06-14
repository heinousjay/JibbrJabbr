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

/**
 * @author jason
 *
 */
public class ParamNode extends TrieNode {
	
	private enum Type {
		Param,
		Splat
	}
	
	
	private SeparatorNode child;
	private final String name;
	private final Type type;
	
	static String makeValue(final String uri, final int start) {
		int end = uri.indexOf(SEPARATOR_STRING, start);
		if (end == -1) { end = uri.length(); }
		return uri.substring(start, end);
	}
		
	ParamNode(final String value) {
		this.name = parseName(value);
		this.type = parseType(value);
	}
	
	private Type parseType(String value) {
		switch (value.charAt(0)) {
		case '*':
			return Type.Splat;
		case ':':
			return Type.Param;
		default:
			throw new AssertionError();
		}
	}
	
	private String parseName(String value) {
		return value.substring(1);
	}

	@Override
	void doAddChild(HttpMethod method, String uri, String destination, int index) {
		if (type == Type.Splat) { 
			throw new IllegalArgumentException(
				"trailing uri after a splat parameter " + method + " for " + uri + " to destination " + destination
			);
		}
		assert uri.charAt(index) == SEPARATOR_CHAR; // must be!
		
		child = child == null ? new SeparatorNode() : child;
		child.addRoute(method, uri, destination, index + 1);
	}

	@Override
	boolean findGoal(RouteFinderContext context, String uri, int index) {
		// first, see if we match
		String matchValue = "";
		switch (type) {
		case Param:
			matchValue = makeValue(uri, index);
			break;
		case Splat:
			matchValue = uri.substring(index);
			break;
		}
		
		if (!matchValue.isEmpty()) {
			context.addParam(name, matchValue);
		}
		
		if (index + matchValue.length() == uri.length()) {
			if (goal != null) {
				context.setGoal(goal);
				return true;
			}
			return false;
		}
		
		if (child != null) {
			return child.findGoal(context, uri, index + matchValue.length() + 1);
		}
		
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(goal: " + goal + ", type: " + type + ", name: " + name + ", child: " + child + ")";
	}
}
