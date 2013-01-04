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
package jj.webbit;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import jj.DateFormatHelper;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.wrapper.HttpResponseWrapper;

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
class JJAccessLoggingHttpHandler implements HttpHandler {
	
	private final Logger access = LoggerFactory.getLogger("access");

	@Override
	public void handleHttpRequest(final HttpRequest request, final HttpResponse response, final HttpControl control) throws Exception {
		
		
	 HttpResponseWrapper responseWrapper = new HttpResponseWrapper(response) {

		 	@Override
		 	public HttpResponseWrapper header(String name, String value) {
		 		if (HttpHeaders.Names.CONTENT_LENGTH.equals(name)) {
		 			request.data(HttpHeaders.Names.CONTENT_LENGTH, value);
		 		}
		 		return super.header(name, value);
		 	}
		 
		 	@Override
		 	public HttpResponseWrapper header(String name, long value) {
		 		if (HttpHeaders.Names.CONTENT_LENGTH.equals(name)) {
		 			request.data(HttpHeaders.Names.CONTENT_LENGTH, value);
		 		}
		 		return super.header(name, value);
		 	}
		 
            @Override
            public HttpResponseWrapper end() {
                log(request, response);
                return super.end();
            }

            @Override
            public HttpResponseWrapper error(Throwable error) {
            	log(request, response);
                return super.error(error);
            }
        };
		control.nextHandler(request, responseWrapper);
	}
	
	private void log(final HttpRequest request, final HttpResponse response) {
		
		access.info("{} - - {} \"{} {} HTTP/1.1\" {} {} {} \"{}\"", 
			extractIP(request.remoteAddress()),
			DateFormatHelper.nowInAccessLogFormat(),
			request.method(),
			request.uri(),
			response.status(),
			request.data(HttpHeaders.Names.CONTENT_LENGTH),
			extractReferer(request),
			request.header(HttpHeaders.Names.USER_AGENT));
	}
	
	private String extractIP(final SocketAddress remoteAddress) {
		
		return (remoteAddress instanceof InetSocketAddress) ? ((InetSocketAddress)remoteAddress).getAddress().getHostAddress() : remoteAddress.toString();
	}
	
	private String extractReferer(final HttpRequest request) {
		
		return request.hasHeader(HttpHeaders.Names.REFERER) ?
			"\"" + request.header(HttpHeaders.Names.REFERER) + "\"" :
			"-";	
	}

}
