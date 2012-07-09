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

import static jj.KernelMessages.ServerErrorFallbackResponse;
import static org.jboss.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import jj.api.EventPublisher;
import jj.api.NonBlocking;
import jj.html.HTMLFragment;
import jj.html.HTMLFragmentFinder;
import net.jcip.annotations.ThreadSafe;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import ch.qos.cal10n.MessageConveyor;

/**
 * <p>
 * handles HTTP requests, ostensibly by searching registered handlers for something that care
 * about this request
 * </p>
 * 
 * @author jason
 *
 */
@ThreadSafe
public class HttpRequestHandler {
	
	private static final HttpResponse RESPONSE_100_CONTINUE = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
	
	
	private final EventPublisher eventPublisher;

	private final byte[] favicon;
	
	private final HttpResponse error503;
	
	public HttpRequestHandler(
		MessageConveyor messageConveyor,
		EventPublisher eventPublisher
	) throws Exception {
		
		assert eventPublisher != null : "eventPublisher is required";
		
		this.eventPublisher = eventPublisher;
		
		error503 = makeFallback503(messageConveyor.getMessage(ServerErrorFallbackResponse));
		
		// totally the wrong place for this as well
		try (InputStream indexStream = HttpRequestHandler.class.getResourceAsStream("builtin/assets/favicon.ico")) {
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
	
	private static URI INDEX;
	private static URI ERROR_404;
	private static URI ERROR_405;
	
	static {
		try {
			INDEX = HttpRequestHandler.class.getResource("/jj/builtin/assets/index.html").toURI();
			ERROR_404 = HttpRequestHandler.class.getResource("/jj/builtin/errors/404.html").toURI();
			ERROR_405 = HttpRequestHandler.class.getResource("/jj/builtin/errors/405.html").toURI();
		} catch (URISyntaxException e) {
			// can't happen
		}
	}
	
	@NonBlocking
	public void handle(final ChannelHandlerContext ctx, final MessageEvent e, final HttpRequest request) throws Exception {
		
		URI resource = null;
		String uri = request.getUri();
		final Channel responseChannel = e.getChannel();
		HttpResponseStatus status = NOT_FOUND;
		if (request.getMethod() != GET) {
			resource = ERROR_405;
			status = METHOD_NOT_ALLOWED;
		} else {
			if (is100ContinueExpected(request)) {
				responseChannel.write(RESPONSE_100_CONTINUE);
			} else if ("/favicon.ico".equals(uri)) {
				writeResponse(responseChannel, request, OK, ChannelBuffers.wrappedBuffer(favicon), "image/vnd.microsoft.icon");
			} else if ("/".equals(uri) || "/index".equals(uri)) {
				resource = INDEX;
				status = OK;
			} else {
				resource = ERROR_404;
			}
		}
		
		if (resource != null) {
			final HttpResponseStatus finalStatus = status;
			try {
				
				new HTMLFragmentFinder(resource) {
					
					@Override
					protected void htmlFragment(HTMLFragment htmlFragment) {
						writeResponse(responseChannel, request, finalStatus, 
							ChannelBuffers.copiedBuffer(
								htmlFragment.element().html(), 
								CharsetUtil.UTF_8
							),
							"text/html; charset=UTF-8"
						);
					}
				};
				
			} catch (Exception ex) {
				eventPublisher.publish(new KernelException(ex));
				write503(responseChannel);
			}
		}
	}
	
	@NonBlocking
	void write503(Channel channel) {
		channel.write(error503).addListener(ChannelFutureListener.CLOSE);
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
}
