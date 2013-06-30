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

import static jj.http.HttpServerInitializer.PipelineStages.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.DateFormatHelper;
import jj.resource.LoadedResource;
import jj.resource.Resource;
import jj.resource.TransferableResource;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.MessageList;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * @author jason
 *
 */
public class JJHttpResponse {

	private static final String MAX_AGE_ONE_YEAR = HttpHeaders.Values.MAX_AGE + "=" + String.valueOf(60 * 60 * 24 * 365);
	
	private static final Logger log = LoggerFactory.getLogger(JJHttpResponse.class);
	
	protected final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	
	private final JJHttpRequest request;
	
	private final Channel channel;
	
	private final Charset charset = StandardCharsets.UTF_8;
	
	private final Logger access;
	
	/**
	 * @param response
	 */
	public JJHttpResponse(final JJHttpRequest request, final Channel channel, final Logger access) {
		this.request = request;
		this.channel = channel;
		this.access = access;
		
		header("X-Request-ID", request.id());
		header("X-Channel-ID", channel.id());
	}
	
	private void maybeClose(final ChannelFuture f) {
		if (!HttpHeaders.isKeepAlive(request.request())) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
		
		log();
	}
	
	public HttpResponseStatus status() {
		return response.getStatus();
	}
	
	/**
	 * sets the status of the outgoing response
	 * @param status
	 * @return
	 */
	public JJHttpResponse status(final HttpResponseStatus status) {
		response.setStatus(status);
		return this;
	}
	
	public JJHttpResponse header(final String name, final String value) {
		response.headers().add(name, value);
		return this;
	}

	public JJHttpResponse headerIfNotSet(final String name, final String value) {
		if (!containsHeader(name)) {
			header(name, value);
		}
		return this;
	}

	public JJHttpResponse headerIfNotSet(final String name, final long value) {
		if (!containsHeader(name)) {
			header(name, value);
		}
		return this;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public boolean containsHeader(String name) {
		return response.headers().contains(name);
	}

	public JJHttpResponse header(final String name, final Date date) {
		response.headers().add(name, date);
		return this;
	}
	
	public JJHttpResponse header(final String name, final long value) {
		response.headers().add(name, value);
		return this;
	}
	/**
	 * @return
	 */
	public List<Entry<String, String>> allHeaders() {
		return response.headers().entries();
	}
	
	public HttpVersion version() {
		return response.getProtocolVersion();
	}
	
	public JJHttpResponse content(final byte[] bytes) {
		response.content().writeBytes(bytes);
		return this;
	}
	
	public JJHttpResponse content(final ByteBuffer buffer) {
		response.content().writeBytes(Unpooled.wrappedBuffer(buffer));
		return this;
	}
	
	public JJHttpResponse end() {
		maybeClose(channel.write(
			MessageList.newInstance(2)
				.add(response)
				.add(LastHttpContent.EMPTY_LAST_CONTENT)
		));
		return this;
	}
	
	public void sendNotFound() {
		sendError(HttpResponseStatus.NOT_FOUND);
	}
	
	public void sendError(final HttpResponseStatus status) {
		byte[] body = status.reasonPhrase().getBytes(StandardCharsets.UTF_8);
		status(status)
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.header(HttpHeaders.Names.CONTENT_LENGTH, body.length)
			.header(HttpHeaders.Names.CONTENT_TYPE, "text/plain; UTF-8")
			.content(body);
		
		maybeClose(channel.write(response));
	}
	
	/**
	 * Sends a 304 Not Modified for the given resource and ends the response,
	 * allowing the result to be cached for one year
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendNotModified(final Resource resource, final boolean cacheable) {
		return status(HttpResponseStatus.NOT_MODIFIED)
			.header(HttpHeaders.Names.CACHE_CONTROL, cacheable ? MAX_AGE_ONE_YEAR : HttpHeaders.Values.NO_CACHE)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.LAST_MODIFIED, resource.lastModifiedDate())
			.end();
	}
	
	private String makeAbsoluteURL(final Resource resource) {
		return new StringBuilder("http")
			.append(request.secure() ? "s" : "")
			.append("://")
			.append(request.host())
			.append(resource.uri())
			.toString();
	}
	
	/**
	 * Sends a 307 Temporary Redirect to the given resource, using the fully qualified
	 * asset URL and not allowing the redirect to be cached
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendTemporaryRedirect(final Resource resource) {
		
		return status(HttpResponseStatus.TEMPORARY_REDIRECT)
			.header(HttpHeaders.Names.LOCATION, makeAbsoluteURL(resource))
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.end();
	}
	
	/**
	 * Responds with the given resource metadata and bytes as a 200 OK, allowing the
	 * result to be cached for one year 
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendCachedResource(final LoadedResource resource) {
		return status(HttpResponseStatus.OK)
			.header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.LAST_MODIFIED, resource.lastModifiedDate())
			.header(HttpHeaders.Names.CONTENT_LENGTH, resource.bytes().limit())
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(resource.bytes())
			.end();
	}
	
	/**
	 * Responds with the given resource and bytes as a 200 OK, not setting any
	 * validation headers and turning caching off if no cache control headers have
	 * previously been set on the response.  this is the appropriate responding
	 * method for dynamically generated responses (not including simple statically
	 * compiled dynamic resources, like less->css)
	 * 
	 * @param resource
	 * @param bytes
	 * @return
	 */
	public JJHttpResponse sendUncachedResource(final LoadedResource resource) {
		
		return status(HttpResponseStatus.OK)
			.headerIfNotSet(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE)
			.header(HttpHeaders.Names.CONTENT_LENGTH, resource.bytes().remaining())
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(resource.bytes())
			.end();
	}

	/**
	 * @param e
	 */
	public JJHttpResponse error(Throwable e) {
		log.error("response ended in error", e);
		sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		return this;
	}

	/**
	 * @return
	 */
	public Charset charset() {
		return charset;
	}

	public String header(String name) {
		return response.headers().get(name);
	}

	/**
	 * @return
	 */
	public String contentsString() {
		return response.content().toString(charset);
	}
	
	public JJHttpResponse sendUncachedResource(TransferableResource resource) throws IOException {
		return header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE)
			.sendCachedResource(resource);
	}

	/**
	 * 
	 * 
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendCachedResource(TransferableResource resource) throws IOException {
		headerIfNotSet(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.LAST_MODIFIED, resource.lastModifiedDate())
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.header(HttpHeaders.Names.CONTENT_LENGTH, resource.size());
		
		MessageList<Object> messageList = 
			MessageList.newInstance(3)
				.add(response)
				.add(resource.fileRegion())
				.add(LastHttpContent.EMPTY_LAST_CONTENT);
		
		channel.pipeline().remove(Compressor.toString());
		
		channel.write(messageList).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (HttpHeaders.isKeepAlive(request.request())) {
					channel.pipeline().addAfter(Encoder.toString(), Compressor.toString(), new HttpContentCompressor());
				} else {
					channel.close();
				}
				log();
			}
		});
		
		return this;
	}
	
	private void log() {
		
		if (access.isInfoEnabled()) {
			access.info("{} - - {} \"{} {} {}\" {} {} {} \"{}\"", 
				extractIP(request.remoteAddress()),
				DateFormatHelper.nowInAccessLogFormat(),
				request.method(),
				request.uri(),
				request.request().getProtocolVersion(),
				response.getStatus(),
				extractContentLength(),
				extractReferer(request),
				request.header(HttpHeaders.Names.USER_AGENT)
			);
		}
		
		if (access.isTraceEnabled()) {
			access.trace("Request Headers");
			for (Entry<String, String> header : request.allHeaders()) {
				access.trace(header.getKey() + " : " + header.getValue());
			}
			
			access.trace("Response Headers");
			for (Entry<String, String> header : response.headers().entries()) {
				access.trace(header.getKey() + " : " + header.getValue());
			}
		}
	}
	
	private String extractIP(final SocketAddress remoteAddress) {
		
		return (remoteAddress instanceof InetSocketAddress) ? 
			((InetSocketAddress)remoteAddress).getAddress().getHostAddress() : 
			remoteAddress.toString();
	}
	
	private String extractReferer(final JJHttpRequest request) {
		
		return request.hasHeader(HttpHeaders.Names.REFERER) ?
			"\"" + request.header(HttpHeaders.Names.REFERER) + "\"" :
			"-";
	}
	
	private String extractContentLength() {
		if (response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
			return String.valueOf(response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
		}
		return "0";
	}
}
