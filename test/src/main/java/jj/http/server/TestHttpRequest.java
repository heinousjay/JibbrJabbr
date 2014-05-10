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
package jj.http.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.inject.Inject;
import javax.inject.Named;

import jj.uri.RouteFinder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;


/**
 * @author jason
 *
 */
public class TestHttpRequest extends AbstractHttpRequest {
	
	public static final String REQUEST_URI = "Request URI";
	

	@Inject
	TestHttpRequest(
		final RouteFinder routeFinder,
		final HttpMethod method,
		final @Named(REQUEST_URI) String uri
	) {
		super(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri), routeFinder);
	}

	private final InetSocketAddress socketAddress = new InetSocketAddress(0);

	public SocketAddress remoteAddress() {
		return socketAddress;
	}

	public HttpHeaders headers() {
		return request.headers();
	}
	
	@Override
	public String toString() {
		
		return new StringBuilder("Test request to ")
			.append(uri())
			.append(" method ")
			.append(method())
			.toString();
	}
}
