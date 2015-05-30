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

import static jj.http.server.PipelineStages.*;
import static jj.server.ServerLocation.Virtual;

import java.util.Set;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.HttpServerResponse;
import jj.http.server.uri.URIMatch;
import jj.resource.ResourceFinder;

/**
 * @author jason
 *
 */
@Singleton
public class WebSocketConnectionMaker {
	
	private final WebSocketFrameHandlerCreator handlerCreator;
	
	private final ResourceFinder resourceFinder;
	
	private final ChannelHandlerContext ctx;
	
	private final FullHttpRequest request;
	
	private final HttpServerResponse response;
	
	private final WebSocketServerHandshakerFactory handshakerFactory;
	
	private final Set<Class<? extends WebSocketConnectionHost>> webSocketConnectionHostClasses;
	
	@Inject
	WebSocketConnectionMaker(
		final WebSocketFrameHandlerCreator handlerCreator,
		final ResourceFinder resourceFinder,
		final ChannelHandlerContext ctx,
		final FullHttpRequest request,
		final HttpServerResponse response,
		final WebSocketServerHandshakerFactory handshakerFactory,
		final Set<Class<? extends WebSocketConnectionHost>> webSocketConnectionHostClasses
	) {
		this.handlerCreator = handlerCreator;
		this.resourceFinder = resourceFinder;
		this.ctx = ctx;
		this.request = request;
		this.response = response;
		this.handshakerFactory = handshakerFactory;
		this.webSocketConnectionHostClasses = webSocketConnectionHostClasses;
	}
	
	public void handshakeWebsocket() {
		final WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request);
		if (handshaker == null) {
			response
				.header(HttpHeaderNames.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue())
				.sendError(HttpResponseStatus.UPGRADE_REQUIRED);
		} else {
			doHandshake(ctx, request, handshaker);
		}
	}

	private void doHandshake(
		final ChannelHandlerContext ctx,
		final FullHttpRequest request,
		final WebSocketServerHandshaker handshaker
	) {
		handshaker.handshake(ctx.channel(), request).addListener(new ChannelFutureListener() {
			
			private boolean isHandshakeFailure(ChannelFuture future) {
				return future.cause() != null &&
					future.cause() instanceof WebSocketHandshakeException;
			}
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					
					URIMatch uriMatch = new URIMatch(request.uri());
					

					WebSocketConnectionHost host = null;
					
					for (Class<? extends WebSocketConnectionHost> hostClass : webSocketConnectionHostClasses) {
						host = resourceFinder.findResource(hostClass, Virtual, uriMatch.name);
						if (host != null) break;
					}
					
					if (host == null) {
						
						// 1011 indicates that a server is terminating the connection because
						// it encountered an unexpected condition that prevented it from
						// fulfilling the request.
						ctx.writeAndFlush(new CloseWebSocketFrame(1011, null)).addListener(CLOSE);
						// TODO: is closing here the right thing? or do we count on the client closing the connection
						// to avoid the time_wait state? 
					
					} else if (!uriMatch.sha1.equals(host.sha1())) {
						
						ctx.writeAndFlush(new TextWebSocketFrame("jj-reload"))
							.addListener(new ChannelFutureListener() {
								
								@Override
								public void operationComplete(ChannelFuture future) throws Exception {
									// 1001 indicates that an endpoint is "going away", such as a server
									// going down or a browser having navigated away from a page.
									ctx.writeAndFlush(new CloseWebSocketFrame(1001, null)).addListener(CLOSE);
									// TODO: is closing here the right thing? or do we count on the client closing the connection
									// to avoid the time_wait state? 
								}
							});
						
						
					} else {
						
						ctx.pipeline().replace(
							JJEngine.toString(),
							JJWebsocketHandler.toString(),
							handlerCreator.createHandler(handshaker, host)
						);
					}
					
				} else if (isHandshakeFailure(future)) {
					response.sendError(HttpResponseStatus.BAD_REQUEST);
				} else {
					ctx.close();
				}
			}
		});
	}
}
