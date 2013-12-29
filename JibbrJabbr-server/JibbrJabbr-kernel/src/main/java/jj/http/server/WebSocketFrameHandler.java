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

import javax.inject.Inject;
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

/**
 * coordinates the management of websocket frames.  mainly the job here
 * is to turn the netty API into something a little easier to use
 * 
 * @author jason
 *
 */
@Singleton
class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private final WebSocketServerHandshaker handshaker;
	
	private final JJWebSocketHandler handler;
	
	private final JJWebSocketConnection connection;
	
	private final WebSocketConnectionTracker connectionTracker;
	
	@Inject
	WebSocketFrameHandler(
		final WebSocketServerHandshaker handshaker,
		final JJWebSocketHandler handler,
		final JJWebSocketConnection connection,
		final WebSocketConnectionTracker connectionTracker
	) {
		this.handshaker = handshaker;
		this.handler = handler;
		this.connection = connection;
		this.connectionTracker = connectionTracker;
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

		connectionTracker.addConnection(connection);
		connection.webSocketConnectionHost().connected(connection);
		handler.opened(connection);
		
		ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				connectionTracker.removeConnection(connection);
				connection.webSocketConnectionHost().disconnected(connection);
				handler.closed(connection);
			}
		});
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		
		connection.markActivity();
		
		// these are in order!
		
		if (frame instanceof TextWebSocketFrame) {
			
			String text = ((TextWebSocketFrame)frame).text();
			if ("jj-hi".equals(text)) {
				ctx.writeAndFlush(new TextWebSocketFrame("jj-yo"));
			} else {
				handler.messageReceived(connection, text);
			}
			
		} else if (frame instanceof PingWebSocketFrame) {
			
			ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
			
		} else if (frame instanceof PongWebSocketFrame) {
				
			handler.ponged(connection, frame.content().retain());
				
		} else if (frame instanceof BinaryWebSocketFrame) {
			
			handler.messageReceived(connection, frame.content().retain());
			
		} else if (frame instanceof CloseWebSocketFrame) {
			
			handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
		
		}
	}
}
