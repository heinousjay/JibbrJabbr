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

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import jj.http.server.methods.HttpMethodHandler;

import com.google.inject.Provider;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * first-chance disposition of an incoming request
 * 
 * basics flow is
 * 
 * - check for decoder errors
 * 
 * - check if the method is one of the methods we are
 *   prepared to handle.  this is determined by having
 *   a registered handler
 * 
 * - add that handler to the pipeline and fire upstream
 * 
 * @author jason
 *
 */
class HttpRequestListeningHandler extends SimpleChannelInboundHandler<HttpRequest> {
	
	private final Map<HttpMethod, Provider<HttpMethodHandler>> methodHandlers;
	
	private String name;
	
	private static final HttpResponse BAD_REQUEST =
		new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);

	private static final HttpResponse NOT_IMPLEMENTED =
		new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED);

	private static final HttpResponse INTERNAL_SERVER_ERROR =
		new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
	
	@Inject
	HttpRequestListeningHandler(
		final Map<HttpMethod, Provider<HttpMethodHandler>> methodHandlers
	) {
		this.methodHandlers = methodHandlers;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
		
		if (request.getDecoderResult().isFailure()) {
			// respond with BAD_REQUEST and close the connection
			// (unless we are being proxied and the connection is keep-alive, that is)
			ctx.writeAndFlush(BAD_REQUEST).addListener(ChannelFutureListener.CLOSE);
			
		} else if (methodHandlers.containsKey(request.getMethod())) {

			HttpMethodHandler methodHandler = methodHandlers.get(request.getMethod()).get();
			methodHandler.request(request);

			ChannelPipeline p = ctx.pipeline();
			p.addAfter(name, "method handler", methodHandler);

			ctx.fireChannelRead(request);

		} else {
			// respond with NOT_IMPLEMENTED
			// unless we are being proxied and the connection is keep-alive
			ctx.writeAndFlush(NOT_IMPLEMENTED).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// publish the error!
		ctx.writeAndFlush(INTERNAL_SERVER_ERROR).addListener(ChannelFutureListener.CLOSE);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		for (Entry<String, ChannelHandler> entry : ctx.pipeline()) {
			if (entry.getValue() == this) {
				name = entry.getKey();
			}
		}
		assert name != null : "couldn't find myself in the pipeline!";
	}

}
