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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.netty.handler.codec.http.HttpMethod;

/**
 * <p>
 * Used to carry the route definition in from the API
 * 
 * <p>
 * for the purposes of implementing hashCode and equals, only
 * the method and URI are considered equivalent
 * 
 * @author jason
 *
 */
public class Route {
	
	private static final Pattern URI_PATTERN = 
		Pattern.compile("^(?:/|(?:/(?:[a-zA-Z0-9.-]+|:[a-zA-Z0-9.$-]+(?:\\([^/]+?\\))?))+(?:/(?:\\*[a-zA-Z0-9.$-]+(?:\\(.+?\\))?)?)?)$");

	private final HttpMethod method;
	private final String uri;
	private final String destination;
	
	private int index = 1;
	
	private List<Parameter> parameters;
	
	private TrieNode parent;
	
	public Route(final HttpMethod method, final String uri, final String destination) {
		
		if (method == null) {
			throw new NullPointerException("method");
		}
		
		if (!URI_PATTERN.matcher(uri).matches()) {
			throw new IllegalArgumentException("uri " + uri + " does not fit the correct pattern");
		}
//		
		// test destination!
		
		this.method = method;
		this.uri = uri;
		this.destination = destination;
	}
	
	//---> accessors
	
	public HttpMethod method() {
		return method;
	}
	
	public String uri() {
		return uri;
	}
	
	public String destination() {
		return destination;
	}
	
	// move this to another class
	public String resolve(Map<String, String> params) {
		// need a local copy for tracking
		// maybe encode them here?
		HashMap<String, String> p = new HashMap<>(params);
		StringBuilder sb = new StringBuilder();
		int current = 0;
		for (Parameter param : parameters) {
			for (; current < param.start; ++current) {
				sb.append(uri.charAt(current));
			}
			// defaults? error?
			sb.append(p.remove(param.name));
			current = param.end;
		}
		for (; current < uri.length(); ++current) {
			sb.append(uri.charAt(current));
		}
		return sb.toString();
	}
	
	//---> internal api
	
	char currentChar() {
		return uri.charAt(index);
	}
	
	int index() {
		return index;
	}
	
	Route advanceIndex() {
		index++;
		return this;
	}
	
	Route advanceIndex(int by) {
		index += by;
		return this;
	}
	
	Route addParam(Parameter parameter) {
		parameters = parameters == null ? new ArrayList<Parameter>() : parameters;
		parameters.add(parameter);
		return this;
	}
	
	Route setParent(TrieNode parent) {
		this.parent = parent;
		this.parameters = this.parameters == null ? null : Collections.unmodifiableList(this.parameters);
		return this;
	}
	
	//---> basics
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Route && equals((Route)obj); 
	}
	
	public boolean equals(Route route) {
		return route != null &&
			method.equals(route.method) &&
			uri.equals(route.uri);
	}
	
	@Override
	public int hashCode() {
		// such a cheat
		return (method + " " + uri).hashCode();
	}
	
	@Override
	public String toString() {
		return "route " + method + " " + uri + " to " + destination + " with params " + parameters;
	}

}
