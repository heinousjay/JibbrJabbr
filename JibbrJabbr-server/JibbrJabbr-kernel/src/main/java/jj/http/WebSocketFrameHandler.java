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
package jj.http;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * @author jason
 *
 */
@Singleton
class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
	
	private static final AttributeKey<JJWebSocketConnection> CONNECTION_ATTRIBUTE_KEY = new AttributeKey<>("connection");

	private final WebSocketServerHandshaker handshaker;
	
	private final JJWebSocketHandler handler;
	
	private final Provider<JJWebSocketConnection> connectionProvider;
	
	@Inject
	WebSocketFrameHandler(
		final WebSocketServerHandshaker handshaker,
		final JJWebSocketHandler handler,
		final Provider<JJWebSocketConnection> connectionProvider
	) {
		this.handshaker = handshaker;
		this.handler = handler;
		this.connectionProvider = connectionProvider;
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		final Attribute<JJWebSocketConnection> connectionAttribute = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY);
		JJWebSocketConnection connection = connectionAttribute.get();
		assert(connection == null) : "new websocket handler instance created and somehow the connection already exists";
		
		connectionAttribute.setIfAbsent(connectionProvider.get());
		handler.onOpen(connectionAttribute.get());
		
		ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				handler.onClose(connectionAttribute.get());
			}
		});
	}
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		
		Attribute<JJWebSocketConnection> connectionAttribute = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY);
		JJWebSocketConnection connection = connectionAttribute.get();
		
		if (frame instanceof CloseWebSocketFrame) {
			
			handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
		
		} else if (frame instanceof PingWebSocketFrame) {
			
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			
		} else if (frame instanceof PongWebSocketFrame) {
				
			handler.onPong(connection, frame.content().array());
				
		} else if (frame instanceof TextWebSocketFrame) {
			
			handler.onMessage(connection, ((TextWebSocketFrame)frame).text());
			
		} else if (frame instanceof BinaryWebSocketFrame) {
			
			handler.onMessage(connection, frame.content().array());
		}
	}
}
