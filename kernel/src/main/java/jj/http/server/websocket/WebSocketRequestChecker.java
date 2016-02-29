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
package jj.http.server.websocket;

import javax.inject.Singleton;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import jj.http.server.uri.URIMatch;

/**
 * @author jason
 *
 */
@Singleton
public class WebSocketRequestChecker {

	private static final String SOCKET = "socket";

	private boolean isWebSocketURI(final FullHttpRequest request) {
		URIMatch uriMatch = new URIMatch(request.uri());
		return SOCKET.equals(uriMatch.extension) && uriMatch.sha1 != null;
	}
	
	private boolean isUpgradeRequest(final FullHttpRequest request) {
		return request.headers().contains(HttpHeaderNames.CONNECTION) &&
			request.headers().get(HttpHeaderNames.CONNECTION).toLowerCase().contains(HttpHeaderValues.UPGRADE.toLowerCase());
	}
	
	public boolean isWebSocketRequest(final FullHttpRequest request) {
		
		return HttpMethod.GET.equals(request.method()) &&
			isUpgradeRequest(request) &&
			HttpHeaderValues.WEBSOCKET.contentEqualsIgnoreCase(request.headers().get(HttpHeaderNames.UPGRADE)) &&
			isWebSocketURI(request);
	}
	
}
