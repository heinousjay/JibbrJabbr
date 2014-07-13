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
package jj.http.server.methods;

import static io.netty.handler.codec.http.HttpMethod.*;

import java.util.Map;

import javax.inject.Inject;

import com.google.inject.Provider;

import jj.http.uri.Router;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @author jason
 *
 */
class OptionsMethodHandler extends HttpMethodHandler {
	
	private final Router router;
	private final Map<HttpMethod, Provider<HttpMethodHandler>> methodHandlers;
	
	@Inject
	OptionsMethodHandler(final Router router, final Map<HttpMethod, Provider<HttpMethodHandler>> methodHandlers) {
		this.router = router;
		this.methodHandlers = methodHandlers;
	}

	@Override
	protected void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request) {
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		StringBuilder sb = new StringBuilder();
		if ("*".equals(request.getUri())) {
			for (HttpMethod method : methodHandlers.keySet()) {
				sb.append(method).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		} else {
			for (HttpMethod method : router.matchURI(OPTIONS, request.getUri()).routes.keySet()) {
				sb.append(method).append(",");
			}
			sb.append(HEAD).append(",").append(TRACE).append(",").append(OPTIONS);
		}
		response.headers().set(HttpHeaders.Names.ALLOW, sb.toString());
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	protected void handleHttpContent(ChannelHandlerContext ctx, HttpContent content) {
		// nein!
	}

}
