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
package jj.http.uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.http.uri.Parameter.Type;

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
		int end = uri.indexOf(PATH_SEPARATOR_STRING, index);
		if (end == -1) { end = uri.indexOf(EXTENSION_SEPARATOR_CHAR, index); }
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
		char current = route.currentChar();
		// this is an assertion because it's a trie construction error
		assert current == PATH_SEPARATOR_CHAR || current == EXTENSION_SEPARATOR_CHAR;
		
		if (terminal) {
			throw new IllegalStateException("only one node type is allowed past an extension. better explanation please!");
		}
		
		if (parameter.type == Type.Splat && current != EXTENSION_SEPARATOR_CHAR) { 
			throw new IllegalArgumentException(
				"only extensions can follow a splat parameter"
			);
		}
		
		child = child == null ? new SeparatorNode(current) : child;
		child.terminal = current == EXTENSION_SEPARATOR_CHAR;
		
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
			int end = uri.length();
			if (child != null) {
				assert child.terminal : "splat followed by non-terminal child! BULLSHIT BRAH";
				end = uri.lastIndexOf('.');
				// if we're expecting an extension an none exist, we don't match.
				// is returning here correct?
				if (end == -1) return false;
			}
			
			matchValue = uri.substring(index, end);
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
	void doCompress() {
		if (child != null) {
			child.compress();
		}
	}
	
	@Override
	void describeChildren(int indent, StringBuilder sb) {
		if (child != null) {
			addIndentation(indent, sb.append("\n")).append(child.separator).append(" = ");
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
