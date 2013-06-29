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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.resource.Resource;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.FileRegion;
import io.netty.channel.MessageList;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
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
	
	private static final Logger logger = LoggerFactory.getLogger(JJHttpResponse.class);
	
	protected final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	
	private final JJHttpRequest request;
	
	private final Channel channel;
	
	private final Charset charset = StandardCharsets.UTF_8;
	
	/**
	 * @param response
	 */
	public JJHttpResponse(final JJHttpRequest request, final Channel channel) {
		this.request = request;
		this.channel = channel;
	}
	
	private void maybeClose() {
		if (!HttpHeaders.isKeepAlive(request.request())) {
			channel.close();
		}
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
	
	public JJHttpResponse content(final byte[] bytes) {
		response.content().writeBytes(bytes);
		return this;
	}
	
	public JJHttpResponse content(final ByteBuffer buffer) {
		response.content().writeBytes(Unpooled.wrappedBuffer(buffer));
		return this;
	}
	
	public JJHttpResponse end() {
		channel.write(
			MessageList.newInstance(2)
				.add(response)
				.add(LastHttpContent.EMPTY_LAST_CONTENT)
		);
		maybeClose();
		return this;
	}
	
	private final byte[] NOT_FOUND = "NOT FOUND!".getBytes(StandardCharsets.UTF_8);
	public void sendNotFound() {
		
		status(HttpResponseStatus.NOT_FOUND)
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.header(HttpHeaders.Names.CONTENT_LENGTH, NOT_FOUND.length)
			.header(HttpHeaders.Names.CONTENT_TYPE, "text/plain; UTF-8");
		
		channel.write(response);
		channel.close();
	}
	
	/**
	 * Sends a 304 Not Modified for the given resource and ends the response,
	 * allowing the result to be cached for one year
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendNotModified(final Resource resource) {
		return status(HttpResponseStatus.NOT_MODIFIED)
			.header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR)
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
	public JJHttpResponse sendCachedResource(final Resource resource, ByteBuffer bytes) {
		return status(HttpResponseStatus.OK)
			.header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.LAST_MODIFIED, resource.lastModifiedDate())
			.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.limit())
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(bytes)
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
	public JJHttpResponse sendUncachedResource(final Resource resource, byte[] bytes) {
		
		return status(HttpResponseStatus.OK)
			.headerIfNotSet(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE)
			.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length)
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(bytes)
			.end();
	}

	/**
	 * @param e
	 */
	public JJHttpResponse error(Throwable e) {
		return this;
	}

	/**
	 * @return
	 */
	public Charset charset() {
		return charset;
	}

	/**
	 * @param cacheControl
	 * @return
	 */
	public String header(String name) {
		return response.headers().get(name);
	}

	/**
	 * @return
	 */
	public String contentsString() {
		return response.content().toString(charset);
	}

	/**
	 * @param fileRegion
	 * @return
	 */
	public JJHttpResponse send(FileRegion fileRegion) {
		MessageList<Object> messageList = 
			MessageList.newInstance(3)
			.add(response)
			.add(fileRegion)
			.add(LastHttpContent.EMPTY_LAST_CONTENT);
		channel.write(messageList);
		maybeClose();
		return this;
	}
}
