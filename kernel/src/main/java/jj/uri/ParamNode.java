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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.uri.Parameter.Type;

/**
 * @author jason
 *
 */
class ParamNode extends TrieNode {
	
	private static final Pattern PARSER = Pattern.compile("^([:*])([\\w\\d$_-]+)(?:\\((.+)\\))?$");
	
	
	private SeparatorNode child;
	final Parameter parameter;
	
	static String makeValue(final Route route) {
		return makeValue(route.uri(), route.index());
	}
	
	static String makeValue(final String uri, final int index) {
		int end = uri.indexOf(SEPARATOR_STRING, index);
		if (end == -1) { end = uri.length(); }
		return uri.substring(index, end);
	}
		
	ParamNode(Route route) {
		String value = makeValue(route);
		Matcher m = PARSER.matcher(value);
		if (!m.matches()) { throw new IllegalArgumentException("[" + value + "] is not a valid parameter definition"); }
		parameter = new Parameter(
			m.group(2),
			route.index(),
			route.index() + value.length(),
			parseType(m.group(1)),
			m.group(3) == null ? null : Pattern.compile("^" + m.group(3) + "$")
		);
		route.addParam(parameter);
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

	@Override
	void doAddChild(Route route) {
		if (parameter.type == Type.Splat) { 
			throw new IllegalArgumentException(
				"trailing uri after a splat parameter in " + route
			);
		}
		assert route.currentChar() == SEPARATOR_CHAR; // must be!
		
		child = child == null ? new SeparatorNode() : child;
		child.addRoute(route.advanceIndex());
	}
	
	private String matchParam(String uri, int index) {
		String value = makeValue(uri, index);
		value = (parameter.pattern != null && !parameter.pattern.matcher(value).find()) ? "" : value;
		return value;
	}

	@Override
	boolean findGoal(RouteFinderContext context, String uri, int index) {
		// first, see if we match
		String matchValue = "";
		switch (parameter.type) {
		case Param:
			matchValue = matchParam(uri, index);
			break;
		case Splat:
			matchValue = uri.substring(index);
			break;
		}
		
		
		if (!matchValue.isEmpty()) {
			
			context.addParam(parameter.name, matchValue);
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
	void compress() {
		if (child != null) {
			child.compress();
		}
		goal = goal == null ? null : Collections.unmodifiableSet(goal);
	}
	
	@Override
	void describeChildren(int indent, StringBuilder sb) {
		if (child != null) {
			addIndentation(indent, sb.append("\n")).append(SEPARATOR_CHAR).append(" = ");
			child.describe(indent, sb);
		}
	}
	
	@Override
	StringBuilder describe(int indent, StringBuilder sb) {
		sb.append(getClass().getSimpleName()).append(" - ").append("parameter: ").append(parameter).append(", goal: ").append(goal);
		describeChildren(indent + 2, sb);
		return sb;
	}
}
