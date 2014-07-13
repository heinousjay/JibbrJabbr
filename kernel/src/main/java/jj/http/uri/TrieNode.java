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

import io.netty.handler.codec.http.HttpMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jason
 *
 */
abstract class TrieNode {

	static final char SEPARATOR_CHAR = '/';
	static final String SEPARATOR_STRING = String.valueOf(SEPARATOR_CHAR);
	static final String PARAM_CHARS = ":*";
	
	
	Map<HttpMethod, Route> goal;
	
	void addRoute(Route route) {
		if (route.uri().length() == route.index()) {
		
			goal = goal == null ? new LinkedHashMap<HttpMethod, Route>(3) : goal;
			if (goal.containsKey(route.method())) {
				throw new IllegalArgumentException(
					"duplicate " + route + ", current config = " + goal
				);
			}
			goal.put(route.method(), route);
			route.setParent(this);
		
		} else {

			doAddChild(route);
		}
	}
	
	abstract void doAddChild(Route route);
	
	abstract boolean findGoal(RouteFinderContext context, String uri, int index);
	
	void compress() {
		doCompress();
		goal = goal == null ? null : Collections.unmodifiableMap(goal);
	}
	
	abstract void doCompress();
	
	abstract StringBuilder describe(int indent, StringBuilder sb);
	
	StringBuilder addIndentation(int indent, StringBuilder sb) {
		char[] c = new char[indent];
		Arrays.fill(c, ' ');
		sb.append(c);
		return sb;
	}
	
	abstract void describeChildren(int indent, StringBuilder sb);
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		describe(0, sb);
		return sb.toString();
	}

}