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
package jj.http;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.DateFormatHelper;
import jj.ExecutionTrace;
import jj.logging.AccessLogger;

import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;

/**
 * logs accesses in combined log format,
 * is totally missing a very important piece
 * because webbit doesn't expose the underlying
 * status line, so we hardcode the http version
 * since we only support modern browsers anyway
 * 
 * @author jason
 *
 */
@Singleton
class JJExecutiveHttpHandler {
	
	private static final String RESPONSE_HEADERS = "Response Headers";
	
	private final Logger access;
	
	private final ExecutionTrace trace;
	
	@Inject
	JJExecutiveHttpHandler(final @AccessLogger Logger access, final ExecutionTrace trace) {
		this.access = access;
		this.trace = trace;
	}

	public void handleHttpRequest(
		final JJHttpRequest request,
		final JJHttpResponse response
	) throws Exception {
		request.data(RESPONSE_HEADERS, new HashMap<String, String>());
		
		try {
			access.error("{}", request);
			trace.start(request, response);
		} catch (Throwable t) {
			access.error("{}", request);
			access.error("{}", t);
		}
	}
	
	private void log(final JJHttpRequest request, final JJHttpResponse response) {
		
		if (access.isInfoEnabled()) {
			access.info("{} - - {} \"{} {} HTTP/1.1\" {} {} {} \"{}\"", 
				extractIP(request.remoteAddress()),
				DateFormatHelper.nowInAccessLogFormat(),
				request.method(),
				request.uri(),
				response.status(),
				header(request, HttpHeaders.Names.CONTENT_LENGTH),
				extractReferer(request),
				request.header(HttpHeaders.Names.USER_AGENT));
		}
		
		if (access.isTraceEnabled()) {
			access.trace("Request Headers");
			for (Entry<String, String> header : request.allHeaders()) {
				access.trace(header.getKey() + " : " + header.getValue());
			}
			
			access.trace("Response Headers");
			for (Entry<String, String> header : rh(request).entrySet()) {
				access.trace(header.getKey() + " : " + header.getValue());
			}
		}
	}
	
	private String extractIP(final SocketAddress remoteAddress) {
		
		return (remoteAddress instanceof InetSocketAddress) ? ((InetSocketAddress)remoteAddress).getAddress().getHostAddress() : remoteAddress.toString();
	}
	
	private String extractReferer(final JJHttpRequest request) {
		
		return request.hasHeader(HttpHeaders.Names.REFERER) ?
			"\"" + request.header(HttpHeaders.Names.REFERER) + "\"" :
			"-";
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, String> rh(final JJHttpRequest request) {
		return ((HashMap<String, String>)request.data(RESPONSE_HEADERS));
	}
	
	private String header(final JJHttpRequest request, final String name) {
		return rh(request).get(name);
	}
	
	private void makeHeader(final JJHttpRequest request, final String name, final String value) {
		rh(request).put(name, value);
	}

}
