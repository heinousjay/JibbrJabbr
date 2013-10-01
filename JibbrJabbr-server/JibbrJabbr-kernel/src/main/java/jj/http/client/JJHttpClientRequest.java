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
package jj.http.client;

import javax.inject.Inject;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import jj.http.AbstractHttpRequest;
import jj.uri.RouteFinder;

/**
 * @author jason
 *
 */
public class JJHttpClientRequest extends AbstractHttpRequest {

	/**
	 * @param request
	 */
	@Inject
	public JJHttpClientRequest(final RouteFinder routeFinder) {
		super(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"), routeFinder);
	}
	
	public JJHttpClientRequest(final String uri, final RouteFinder routeFinder) {
		super(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri), routeFinder);
	}
	
	public JJHttpClientRequest(final HttpMethod method, final String uri, final RouteFinder routeFinder) {
		super(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri), routeFinder);
	}
 
	public JJHttpClientRequest uri(String uri) {
		request.setUri(uri);
		return this;
	}
	
	public JJHttpClientRequest method(HttpMethod method) {
		request.setMethod(method);
		return this;
	}
	
	public JJHttpClientRequest header(String name, String value) {
		super.header(name, value);
		return this;
	}
}
