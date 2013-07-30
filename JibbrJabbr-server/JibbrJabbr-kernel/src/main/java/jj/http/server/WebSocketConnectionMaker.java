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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.script.AssociatedScriptBundle;
import jj.script.ScriptBundleFinder;
import jj.uri.URIMatch;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * @author jason
 *
 */
@Singleton
class WebSocketConnectionMaker {
	
	private static final Pattern HTTP_REPLACER = Pattern.compile("http");
	
	private final Injector parentInjector;
	
	private final ScriptBundleFinder scriptBundleFinder;
	
	private final ChannelHandlerContext ctx;
	
	private final FullHttpRequest request;
	
	@Inject
	WebSocketConnectionMaker(
		final Injector parentInjector,
		final ScriptBundleFinder scriptBundleFinder,
		final ChannelHandlerContext ctx,
		final FullHttpRequest request
	) {
		this.parentInjector = parentInjector;
		this.scriptBundleFinder = scriptBundleFinder;
		this.ctx = ctx;
		this.request = request;
	}
	
	void handshakeWebsocket() {
		// this may or may not always be right... but I also don't really care.
		final String uri = 
			HTTP_REPLACER.matcher(request.headers().get(HttpHeaders.Names.ORIGIN) + request.getUri()).replaceFirst("ws");
		
		WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(uri, null, false);
		final WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
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
					final AssociatedScriptBundle scriptBundle = scriptBundleFinder.forURIMatch(uriMatch);
					
					if (scriptBundle == null) {
						
						ctx.channel().writeAndFlush(new TextWebSocketFrame("jj-reload"))
							.addListener(new ChannelFutureListener() {
								
								@Override
								public void operationComplete(ChannelFuture future) throws Exception {
									handshaker.close(ctx.channel(), new CloseWebSocketFrame(1000, null));
								}
							});
						
						
					} else {
					
						Injector injector = parentInjector.createChildInjector(new AbstractModule() {
							@Override
							protected void configure() {
								bind(JJWebSocketConnection.class);
								bind(WebSocketFrameHandler.class);
								bind(WebSocketServerHandshaker.class).toInstance(handshaker);
								bind(AssociatedScriptBundle.class).toInstance(scriptBundle);
							}
						});
						
						ctx.pipeline().replace(
							JJEngine.toString(),
							JJWebsocketHandler.toString(),
							injector.getInstance(WebSocketFrameHandler.class)
						);
					}
					
				} else {
					ctx.channel().close();
				}
			}
		});
	}
}
