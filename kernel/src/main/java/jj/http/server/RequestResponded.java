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

import jj.DateFormatHelper;
import jj.http.HttpRequest;
import jj.logging.LoggedEvent;

/**
 *
 * 
 * @author jason
 *
 */
@AccessLogger
class RequestResponded implements LoggedEvent {
	
	private final JJHttpServerRequest request;
	private final JJHttpServerResponse response;
	
	RequestResponded(JJHttpServerRequest request, JJHttpServerResponse response) {
		this.request = request;
		this.response = response;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.info(
			"request for [{}] completed in {} milliseconds (wall time) (stats events!)",
			request.uri(),
			request.wallTime()
		);
		
		if (logger.isInfoEnabled()) {
			logger.info("{} - - {} \"{} {} {}\" {} {} {} \"{}\"", 
				extractIP(request.remoteAddress()),
				DateFormatHelper.nowInAccessLogFormat(),
				request.method(),
				request.request().getUri(),
				request.request().getProtocolVersion(),
				response.status().code(),
				extractContentLength(),
				extractReferer(request),
				request.header(HttpHeaders.Names.USER_AGENT)
			);
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("Request Headers");
			for (Entry<String, String> header : request.allHeaders()) {
				logger.trace(header.getKey() + " : " + header.getValue());
			}
			
			logger.trace("Response Headers");
			for (Entry<String, String> header : response.allHeaders()) {
				logger.trace(header.getKey() + " : " + header.getValue());
			}
		}
	}
	

	
	private String extractIP(final SocketAddress remoteAddress) {
		
		return (remoteAddress instanceof InetSocketAddress) ? 
			((InetSocketAddress)remoteAddress).getAddress().getHostAddress() : 
			remoteAddress.toString();
	}
	
	private String extractReferer(final HttpRequest request) {
		
		return request.hasHeader(HttpHeaders.Names.REFERER) ?
			"\"" + request.header(HttpHeaders.Names.REFERER) + "\"" :
			"-";
	}
	
	private String extractContentLength() {
		if (response.containsHeader(HttpHeaders.Names.CONTENT_LENGTH)) {
			return response.header(HttpHeaders.Names.CONTENT_LENGTH);
		}
		return "0";
	}

}
