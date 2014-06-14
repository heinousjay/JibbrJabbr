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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * state carrier for route matching attempts
 * 
 * @author jason
 *
 */
class RouteFinderContext {
	
	class Match {
		Map<String, String> params;
		Map<HttpMethod, String> goal;
		
		void addParam(String key, String value) {
			if (params == null) { params = new HashMap<>(); }
			params.put(key, value);
		}
	}

	/** a stack of separator nodes */
	// no, this needs to maintain more context...
	ArrayDeque<SeparatorNode> lastSeparators = new ArrayDeque<>(4);
	
	/** the goals we have matched thus far */
	List<Match> matches = new ArrayList<>();
	
	Match currentMatch;
	
	void addParam(String key, String value) {
		if (currentMatch == null) { currentMatch = new Match(); }
		currentMatch.addParam(key, value);
	}
	
	void setGoal(Map<HttpMethod, String> goal) {
		Match match = currentMatch == null ? new Match() : currentMatch;
		match.goal = goal;
		matches.add(match);
		currentMatch = null;
	}
}
