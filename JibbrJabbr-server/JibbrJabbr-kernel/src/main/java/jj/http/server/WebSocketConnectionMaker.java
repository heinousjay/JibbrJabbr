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

import static jj.http.server.PipelineStages.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.ResourceFinder;
import jj.resource.document.DocumentScriptEnvironment;
import jj.uri.URIMatch;

/**
 * @author jason
 *
 */
@Singleton
class WebSocketConnectionMaker {
	
	private final WebSocketFrameHandlerCreator handlerCreator;
	
	private final ResourceFinder resourceFinder;
	
	private final ChannelHandlerContext ctx;
	
	private final FullHttpRequest request;
	
	private final WebSocketServerHandshakerFactory handshakerFactory;
	
	@Inject
	WebSocketConnectionMaker(
		final WebSocketFrameHandlerCreator handlerCreator,
		final ResourceFinder resourceFinder,
		final ChannelHandlerContext ctx,
		final FullHttpRequest request,
		final WebSocketServerHandshakerFactory handshakerFactory
	) {
		this.handlerCreator = handlerCreator;
		this.resourceFinder = resourceFinder;
		this.ctx = ctx;
		this.request = request;
		this.handshakerFactory = handshakerFactory;
	}
	
	void handshakeWebsocket() {
		final WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request);
		if (handshaker == null) {
			HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UPGRADE_REQUIRED);
	        res.headers().set(Names.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
	        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
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
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					
					URIMatch uriMatch = new URIMatch(request.getUri());
					final DocumentScriptEnvironment scriptEnvironment =
						resourceFinder.findResource(DocumentScriptEnvironment.class, uriMatch.name);
					
					if (scriptEnvironment == null) {
						
						// 1011 indicates that a server is terminating the connection because
					    // it encountered an unexpected condition that prevented it from
					    // fulfilling the request.
						ctx.writeAndFlush(new CloseWebSocketFrame(1011, null)).addListener(CLOSE);
					
					} else if (!uriMatch.sha1.equals(scriptEnvironment.sha1())) {
						
						ctx.writeAndFlush(new TextWebSocketFrame("jj-reload"))
							.addListener(new ChannelFutureListener() {
								
								@Override
								public void operationComplete(ChannelFuture future) throws Exception {
									// 1001 indicates that an endpoint is "going away", such as a server
								    // going down or a browser having navigated away from a page.
									ctx.writeAndFlush(new CloseWebSocketFrame(1001, null)).addListener(CLOSE);
								}
							});
						
						
					} else {
						
						ctx.pipeline().replace(
							JJEngine.toString(),
							JJWebsocketHandler.toString(),
							handlerCreator.createHandler(handshaker, scriptEnvironment)
						);
					}
					
				} else {
					ctx.close();
				}
			}
		});
	}
}
