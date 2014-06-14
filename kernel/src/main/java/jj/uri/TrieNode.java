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

/**
 * @author jason
 *
 */
abstract class TrieNode {

	static final char SEPARATOR_CHAR = '/';
	static final String SEPARATOR_STRING = String.valueOf(SEPARATOR_CHAR);
	static final String PARAM_CHARS = ":*";
	
	
	Map<HttpMethod, String> goal;
	
	void addRoute(HttpMethod method, String uri, String destination, int index) {
		if (uri.length() == index) {
		
			doAddGoal(method, uri, destination);
		
		} else {

			doAddChild(method, uri, destination, index);
		}
	}
	

	void doAddGoal(HttpMethod method, String uri, String destination) {
		goal = goal == null ? new HashMap<HttpMethod, String>(3) : goal;
		if (goal.containsKey(method)) {
			throw new IllegalArgumentException(
				"duplicate route " + method + " for " + uri + ", new destination = " + destination + ", current config = " + goal
			);
		}
		goal.put(method, destination);
	}
	
	abstract void doAddChild(HttpMethod method, String uri, String destination, int index);
	
	abstract boolean findGoal(RouteFinderContext context, String uri, int index);

}