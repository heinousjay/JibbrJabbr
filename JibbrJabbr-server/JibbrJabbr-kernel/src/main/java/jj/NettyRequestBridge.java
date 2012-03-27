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
import java.util.concurrent.ExecutorService;

import jj.html.HTMLFragment;

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
import org.jboss.netty.handler.codec.http.HttpMethod;
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

	// still doesn't belong here but it's simpler to move out now
	
	private static final HTMLFragment index;
	private static final HTMLFragment error404;
	private static final HTMLFragment error405;
	
	static {
		Path jar = JJ.jarForClass(NettyRequestBridge.class);
		try (FileSystem jarFS = FileSystems.newFileSystem(jar, null)) { 
			index = new HTMLFragment(jarFS.getPath("jj", "assets", "index.html"));
			error404 = new HTMLFragment(jarFS.getPath("jj", "errors", "404.html"));
			error405 = new HTMLFragment(jarFS.getPath("jj", "errors", "405.html"));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} 
	}
	
	private final LocLogger logger;

	private final ExecutorService requestExecutor;
	
	private final Kernel.Controller controller;

	private final byte[] favicon;
	
	private final HttpResponse error503;

	public NettyRequestBridge(
		final MessageConveyor messageConveyor,
		final LocLogger logger,
		final KernelThreadPoolExecutor requestExecutor,
		final Kernel.Controller controller
	) throws Exception {

		assert messageConveyor != null;
		assert logger != null;
		assert requestExecutor != null;
		assert controller != null;
		
		logger.debug(ObjectInstantiating, NettyRequestBridge.class);

		this.logger = logger;
		this.requestExecutor = requestExecutor;
		this.controller = controller;
		
		error503 = new DefaultHttpResponse(HTTP_1_1, SERVICE_UNAVAILABLE);
		error503.setHeader(CONTENT_TYPE, "text/html; charset=" + CharsetUtil.UTF_8.name());
		error503.setContent(
			ChannelBuffers.wrappedBuffer(
				messageConveyor.getMessage(ServerErrorFallbackResponse).getBytes(CharsetUtil.UTF_8)
			)
		);
		// did this work?
		logger.info(messageConveyor.getMessage(ServerErrorFallbackResponse));

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

	@Override
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
					requestExecutor.execute(new HttpResponseTask(request, e.getChannel()));
				}
			} else if (msg instanceof WebSocketFrame) {
				handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			}
		} else {
			e.getChannel()
				.write(error503)
				.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private static final String WEBSOCKET_URI = "/websocket";

	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_URI;
	}

	// handle this in the I/O thread for now
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

	private final class HttpResponseTask implements Runnable {

		private final HttpRequest request;
		private final String uri;
		private final HttpMethod method;
		private final Channel responseChannel;

		HttpResponseTask(final HttpRequest request, final Channel responseChannel) {
			this.request = request;
			this.uri = request.getUri();
			this.method = request.getMethod();
			this.responseChannel = responseChannel;
		}

		@Override
		public void run() {
			logger.info("Servicing {} for {}", method, uri);

			if (GET != method) {
				send405();
			} else {
				if (is100ContinueExpected(request)) {
					send100Continue();
				} if ("/favicon.ico".equals(uri)) {
					sendFavicon();
				} else if ("/".equals(uri) || "/index".equals(uri)) {
					sendIndex();
				} else {
					send404();
				}
			}
		}

		private void send100Continue() { // no need to be fancy here
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
			responseChannel.write(response);
		}

		private void send404() {
			writeResponse(NOT_FOUND, ChannelBuffers.copiedBuffer(error404.element().html(), CharsetUtil.UTF_8),
				"text/html; charset=UTF-8");
		}

		private void send405() {
			writeResponse(METHOD_NOT_ALLOWED, ChannelBuffers.copiedBuffer(error405.element().html(), CharsetUtil.UTF_8),
				"text/html; charset=UTF-8");
		}

		private void sendFavicon() {
			writeResponse(OK, ChannelBuffers.wrappedBuffer(favicon), "image/vnd.microsoft.icon");
		}

		private void sendIndex() {
			writeResponse(OK, ChannelBuffers.copiedBuffer(index.element().html(), CharsetUtil.UTF_8), "text/html; charset=UTF-8");
		}

		private void writeResponse(HttpResponseStatus status, ChannelBuffer content, String contentType) {
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
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// do something ridiculous
		// logging as an error is probably not the right thing
		logger.warn("Exception during I/O, dropping the channel", e.getCause());
		e.getChannel().close();
	}
}
