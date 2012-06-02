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
import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import jj.html.HTMLFragment;
import jj.html.HTMLFragmentFinder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

/**
 * Pulls the 
 * @author Jason Miller
 *
 */
public class NettyRequestBridge extends SimpleChannelUpstreamHandler {
	
	private final LocLogger logger;
	
	private final Kernel.Controller controller;
	
	private final HTMLFragmentFinder htmlFragmentFinder;

	private final byte[] favicon;
	
	private final HttpResponse error503;

	public NettyRequestBridge(
		final MessageConveyor messageConveyor,
		final LocLogger logger,
		final Kernel.Controller controller,
		final HTMLFragmentFinder htmlFragmentFinder
	) throws Exception {

		assert messageConveyor != null : "messageConveyor is required";
		assert logger != null : "logger is required";
		assert controller != null : "controller is required";
		assert htmlFragmentFinder != null : "htmlFragmentFinder is required";
		
		logger.debug(ObjectInstantiating, NettyRequestBridge.class);

		this.logger = logger;
		this.controller = controller;
		this.htmlFragmentFinder = htmlFragmentFinder;
		
		error503 = makeFallback503(messageConveyor.getMessage(ServerErrorFallbackResponse));
		
		// totally the wrong place for this as well
		try (InputStream indexStream = NettyRequestBridge.class.getResourceAsStream("assets/favicon.ico")) {
			assert indexStream != null;
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024)) {
				byte[] buffer = new byte[1024];
				int read = -1;
				while ((read = indexStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, read);
				}
				favicon = outputStream.toByteArray();
			}
		}
	}
	
	@NonBlocking
	private HttpResponse makeFallback503(String responseString) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, SERVICE_UNAVAILABLE);
		response.setHeader(CONTENT_TYPE, "text/html; charset=" + CharsetUtil.UTF_8.name());
		response.setContent(
			ChannelBuffers.wrappedBuffer(
				responseString.getBytes(CharsetUtil.UTF_8)
			)
		);
		
		return response;
	}

	@Override
	@NonBlocking
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		// if we're paused, return a 503 directly
		if (controller.clearToServe()) {
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
					handleHttp(ctx, e, request);
				}
			} else if (msg instanceof WebSocketFrame) {
				handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			}
		} else {
			write503(e.getChannel());
		}
	}
	
	private void write503(Channel channel) {
		channel.write(error503)
			.addListener(ChannelFutureListener.CLOSE);
	}
	
	private static final HttpResponse RESPONSE_100_CONTINUE = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
	
	@NonBlocking
	private void handleHttp(final ChannelHandlerContext ctx, final MessageEvent e, final HttpRequest request) throws Exception {
		// this totally does not belong here.
		// need to set up a system of handlers
		String path = null;
		String uri = request.getUri();
		final Channel responseChannel = e.getChannel();
		HttpResponseStatus status = NOT_FOUND;
		if (request.getMethod() != GET) {
			path = "errors/405.html";
			status = METHOD_NOT_ALLOWED;
		} else {
			if (is100ContinueExpected(request)) {
				responseChannel.write(RESPONSE_100_CONTINUE);
			} else if ("/favicon.ico".equals(uri)) {
				writeResponse(responseChannel, request, OK, ChannelBuffers.wrappedBuffer(favicon), "image/vnd.microsoft.icon");
			} else if ("/".equals(uri) || "/index".equals(uri)) {
				path = "assets/index.html";
				status = OK;
			} else {
				path = "errors/404.html";
			}
		}
		
		if (path != null) {
			final HttpResponseStatus finalStatus = status;
			try {
				FileSystem jarFS = FileSystems.newFileSystem(JJ.jarForClass(NettyRequestBridge.class), null);
				htmlFragmentFinder.find(jarFS.getPath("jj"), path, new SynchronousOperationCallback<HTMLFragment>() {
					
					@Override
					public void complete(HTMLFragment htmlFragment) {
						writeResponse(responseChannel, request, finalStatus, 
							ChannelBuffers.copiedBuffer(
								htmlFragment.element().html(), 
								CharsetUtil.UTF_8
							),
							"text/html; charset=UTF-8"
						);
					}
					
					@Override
					public void throwable(Throwable t) {
						logger.error("NOT GOOD", t);
						write503(responseChannel);
					}
				});
			} catch (Exception ex) {
				logger.error("NOT GOOD", ex);
				
			}
		}
	}
	
	@NonBlocking
	private void writeResponse(
		final Channel responseChannel,
		final HttpRequest request,
		final HttpResponseStatus status,
		final ChannelBuffer content,
		final String contentType
	) {
		// Decide whether to close the connection or not.
		boolean keepAlive = isKeepAlive(request);

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setContent(content);
		response.setHeader(CONTENT_TYPE, contentType);

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.setHeader(CONTENT_LENGTH, content.readableBytes());
		}

		// Write the response.
		ChannelFuture future = responseChannel.write(response);

		// Close the non-keep-alive connection after the write operation is
		// done.
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
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
		// do something ridiculous
		// logging as an error is probably not the right thing
		logger.warn("Exception during I/O, dropping the channel", e.getCause());
		e.getChannel().close();
	}
}
