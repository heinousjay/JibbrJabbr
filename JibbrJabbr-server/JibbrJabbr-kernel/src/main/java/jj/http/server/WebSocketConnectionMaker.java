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

import static jj.http.server.HttpServerChannelInitializer.PipelineStages.*;
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

import jj.script.DocumentScriptExecutionEnvironment;
import jj.script.ScriptExecutionEnvironmentFinder;
import jj.uri.URIMatch;

/**
 * @author jason
 *
 */
@Singleton
class WebSocketConnectionMaker {
	
	private final WebSocketFrameHandlerCreator handlerCreator;
	
	private final ScriptExecutionEnvironmentFinder scriptExecutionEnvironmentFinder;
	
	private final ChannelHandlerContext ctx;
	
	private final FullHttpRequest request;
	
	private final WebSocketServerHandshakerFactory handshakerFactory;
	
	@Inject
	WebSocketConnectionMaker(
		final WebSocketFrameHandlerCreator handlerCreator,
		final ScriptExecutionEnvironmentFinder scriptExecutionEnvironmentFinder,
		final ChannelHandlerContext ctx,
		final FullHttpRequest request,
		final WebSocketServerHandshakerFactory handshakerFactory
	) {
		this.handlerCreator = handlerCreator;
		this.scriptExecutionEnvironmentFinder = scriptExecutionEnvironmentFinder;
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
					final DocumentScriptExecutionEnvironment scriptExecutionEnvironment = scriptExecutionEnvironmentFinder.forURIMatch(uriMatch);
					
					if (scriptExecutionEnvironment == null) {
						
						ctx.writeAndFlush(new TextWebSocketFrame("jj-reload"))
							.addListener(new ChannelFutureListener() {
								
								@Override
								public void operationComplete(ChannelFuture future) throws Exception {
									ctx.writeAndFlush(new CloseWebSocketFrame(1000, null)).addListener(CLOSE);
								}
							});
						
						
					} else {
						
						ctx.pipeline().replace(
							JJEngine.toString(),
							JJWebsocketHandler.toString(),
							handlerCreator.createHandler(handshaker, scriptExecutionEnvironment)
						);
					}
					
				} else {
					ctx.close();
				}
			}
		});
	}
}
