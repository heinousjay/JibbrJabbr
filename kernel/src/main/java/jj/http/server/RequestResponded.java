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

import io.netty.handler.codec.http.HttpHeaders;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map.Entry;

import org.slf4j.Logger;

import jj.logging.LoggedEvent;
import jj.util.DateFormatHelper;

/**
 *
 * 
 * @author jason
 *
 */
@AccessLogger
class RequestResponded extends LoggedEvent {
	
	private final HttpServerRequestImpl request;
	private final HttpServerResponseImpl response;
	
	RequestResponded(HttpServerRequestImpl request, HttpServerResponseImpl response) {
		this.request = request;
		this.response = response;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.info(
			"request for [{}] completed in {} milliseconds (wall time) (stats events!)",
			request.uriMatch().uri,
			request.wallTime()
		);
		
		if (logger.isInfoEnabled()) {
			logger.info("{} - - {} \"{} {} {}\" {} {} {} {}", 
				extractIP(request.remoteAddress()),
				DateFormatHelper.nowInAccessLogFormat(),
				request.method(),
				request.request().uri(),
				request.request().protocolVersion(),
				response.status().code(),
				extractContentLength(),
				extractReferer(request),
				extractUserAgent(request)
			);
		}
		
		if (logger.isTraceEnabled()) {
			StringBuilder output = new StringBuilder("Request Headers:");
			for (Entry<CharSequence, CharSequence> header : request.allHeaders()) {
				output.append("\n").append(header.getKey()).append(" : ").append(header.getValue());
			}
			
			logger.trace("{}\n", output);
			
			output = new StringBuilder("Response Headers:");
			for (Entry<CharSequence, CharSequence> header : response.allHeaders()) {
				output.append("\n").append(header.getKey()).append(" : ").append(header.getValue());
			}
			logger.trace("{}\n", output);
		}
	}
	

	
	private String extractIP(final SocketAddress remoteAddress) {
		
		return (remoteAddress instanceof InetSocketAddress) ? 
			((InetSocketAddress)remoteAddress).getAddress().getHostAddress() : 
			remoteAddress.toString();
	}
	
	private String extractReferer(final HttpServerRequest request) {
		return request.hasHeader(HttpHeaders.Names.REFERER) ?
			"\"" + request.header(HttpHeaders.Names.REFERER) + "\"" :
			"-";
	}
	
	private String extractUserAgent(final HttpServerRequest request) {
		return request.hasHeader(HttpHeaders.Names.USER_AGENT) ?
			"\"" + request.header(HttpHeaders.Names.USER_AGENT) + "\"" :
			"-";
	}
	
	private CharSequence extractContentLength() {
		if (response.containsHeader(HttpHeaders.Names.CONTENT_LENGTH)) {
			return response.header(HttpHeaders.Names.CONTENT_LENGTH);
		}
		return "0";
	}

}
