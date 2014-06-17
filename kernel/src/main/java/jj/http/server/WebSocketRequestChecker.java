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

import javax.inject.Singleton;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import jj.http.server.uri.URIMatch;

/**
 * @author jason
 *
 */
@Singleton
class WebSocketRequestChecker {

	private static final String SOCKET = "socket";

	private boolean isWebSocketURI(final FullHttpRequest request) {
		URIMatch uriMatch = new URIMatch(request.getUri());
		return SOCKET.equals(uriMatch.extension) && uriMatch.sha1 != null;
	}
	
	private boolean isUpgradeRequest(final FullHttpRequest request) {
		return request.headers().contains(HttpHeaders.Names.CONNECTION) &&
			request.headers().get(HttpHeaders.Names.CONNECTION).toLowerCase().contains(HttpHeaders.Values.UPGRADE.toLowerCase());
	}
	
	boolean isWebSocketRequest(final FullHttpRequest request) {
		
		return HttpMethod.GET.equals(request.getMethod()) &&
			isUpgradeRequest(request) &&
			HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(request.headers().get(HttpHeaders.Names.UPGRADE)) &&
			isWebSocketURI(request);
	}
	
}
