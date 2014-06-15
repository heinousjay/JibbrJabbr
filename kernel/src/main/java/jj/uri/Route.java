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

import java.util.regex.Pattern;

import io.netty.handler.codec.http.HttpMethod;

/**
 * Used to carry the route definition in from the API
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
	
	public Route(final HttpMethod method, final String uri, final String destination) {
		
		if (!URI_PATTERN.matcher(uri).matches()) {
			throw new IllegalArgumentException("uri " + uri + " does not fit the correct pattern");
		}
		
		this.method = method;
		this.uri = uri;
		this.destination = destination;
	}
	
	public HttpMethod method() {
		return method;
	}
	
	public String uri() {
		return uri;
	}
	
	public String destination() {
		return destination;
	}
	
	@Override
	public String toString() {
		return "route " + method + " " + uri + " to " + destination + ";";
	}

}
