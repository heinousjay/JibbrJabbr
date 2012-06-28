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
package jj;

import static jj.KernelMessages.*;
import jj.api.NonBlocking;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.cal10n.LocLogger;

/**
 * Pulls the 
 * @author Jason Miller
 *
 */
public class NettyRequestBridge extends SimpleChannelUpstreamHandler {
	
	private final LocLogger logger;
	
	private final HttpRequestHandler httpRequestHandler;

	public NettyRequestBridge(
		final LocLogger logger,
		final HttpRequestHandler httpRequestHandler
	) throws Exception {

		assert logger != null : "logger is required";
		assert (httpRequestHandler != null) : "httpRequestHandler is required";
		
		logger.debug(ObjectInstantiating, NettyRequestBridge.class);

		this.logger = logger;
		this.httpRequestHandler = httpRequestHandler;
		
	}

	@Override
	@NonBlocking
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		// if we're paused, return a 503 directly
		//if (controller.clearToServe()) {
			// if it's a websocket request, adjust the pipeline, then... i dunno
			// otherwise make a kernel task to process it
			Object msg = e.getMessage();
			if (msg instanceof HttpRequest) {
				HttpRequest request = (HttpRequest)e.getMessage();
				if (WEBSOCKET_URI.equals(request.getUri())) {
					// Handshake
					WebSocketServerHandshakerFactory wsFactory = 
						new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false);
					WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
					if (handshaker == null) {
						wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
					} else {
						handshaker.handshake(ctx.getChannel(), request);
						ctx.setAttachment(handshaker);
					}
				} else {
					httpRequestHandler.handle(ctx, e, request);
				}
			} else if (msg instanceof WebSocketFrame) {
				handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			}
		//} else {
		//	httpRequestHandler.write503(e.getChannel());
		//}
	}
	
	

	private static final String WEBSOCKET_URI = "/websocket";

	@NonBlocking
	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_URI;
	}


	@NonBlocking
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			((WebSocketServerHandshaker)ctx.getAttachment()).close(ctx.getChannel(), (CloseWebSocketFrame) frame);

		} else if (frame instanceof PingWebSocketFrame) {
			ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));

		} else {
			
			// Send the uppercase string back.
			String request = ((TextWebSocketFrame) frame).getText();
			logger.debug("Channel {} received {}", ctx.getChannel().getId(), request);
			ctx.getChannel().write(new TextWebSocketFrame(request.toUpperCase()));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// publish the exception event
		logger.warn("Exception during I/O, dropping the channel", e.getCause());
		e.getChannel().close();
	}
}
