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

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import jj.ServerLogger;
import jj.logging.LoggedEvent;
import org.slf4j.Logger;

/**
 * I know this seems like a waste of a class since it thinly and
 * leakily wraps the underlying netty objects but the point here is
 * to use this to help wedge apart the use of FullHttpRequest internally
 * and i don't want to have a bunch of tests to update AGAIN when that
 * happens, so this is here just to hide that one detail
 * 
 * @author jason
 *
 */
@ServerLogger
public class EmbeddedHttpRequest extends LoggedEvent {
	
	final DefaultHttpRequest request;

	public EmbeddedHttpRequest(final String uri) {
		this(HttpMethod.GET, uri);
	}
	
	public EmbeddedHttpRequest(final HttpMethod method, final String uri) {
		this(HttpVersion.HTTP_1_1, method, uri);
	}
	
	public EmbeddedHttpRequest(final HttpVersion httpVersion, final HttpMethod method, final String uri) {
		request = new DefaultHttpRequest(httpVersion, method, uri);
		request.headers().add(HttpHeaderNames.HOST, "localhost");
	}
	
	public HttpHeaders headers() {
		return request.headers();
	}
	
	FullHttpRequest fullHttpRequest() {
		DefaultFullHttpRequest result = 
			new DefaultFullHttpRequest(request.protocolVersion(), request.method(), request.uri());
		result.headers().add(request.headers());
		return result;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.debug("processing embedded request {}" , request);
	}
}
