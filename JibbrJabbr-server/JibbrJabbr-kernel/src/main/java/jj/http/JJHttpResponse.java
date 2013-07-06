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

import static jj.http.HttpServerChannelInitializer.PipelineStages.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.DateFormatHelper;
import jj.Version;
import jj.logging.AccessLogger;
import jj.resource.LoadedResource;
import jj.resource.Resource;
import jj.resource.TransferableResource;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;
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
@Singleton
public class JJHttpResponse {
	
	private static final String SERVER_NAME = String.format(
		"%s/%s (%s)",
		Version.name,
		Version.version,
		Version.branchName		
	);

	private static final String MAX_AGE_ONE_YEAR = HttpHeaders.Values.MAX_AGE + "=" + String.valueOf(60 * 60 * 24 * 365);
	
	private static final Logger log = LoggerFactory.getLogger(JJHttpResponse.class);
	
	protected final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	
	private final JJHttpRequest request;
	
	private final Channel channel;
	
	private final Charset charset = StandardCharsets.UTF_8;
	
	private final Logger access;
	
	private volatile boolean isCommitted = false;
	
	/**
	 * @param response
	 */
	@Inject
	public JJHttpResponse(
		final JJHttpRequest request,
		final Channel channel,
		final @AccessLogger Logger access
	) {
		this.request = request;
		this.channel = channel;
		this.access = access;
		header(HttpHeaders.Names.SERVER, SERVER_NAME);
	}
	
	private void assertNotCommitted() {
		assert !isCommitted : "response has already been committed.  modification is not permitted";
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
		assertNotCommitted();
		response.setStatus(status);
		return this;
	}
	
	public JJHttpResponse header(final String name, final String value) {
		assertNotCommitted();
		response.headers().add(name, value);
		return this;
	}

	public JJHttpResponse headerIfNotSet(final String name, final String value) {
		assertNotCommitted();
		if (!containsHeader(name)) {
			header(name, value);
		}
		return this;
	}

	public JJHttpResponse headerIfNotSet(final String name, final long value) {
		assertNotCommitted();
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
		assertNotCommitted();
		response.headers().add(name, date);
		return this;
	}
	
	public JJHttpResponse header(final String name, final long value) {
		assertNotCommitted();
		response.headers().add(name, value);
		return this;
	}
	/**
	 * @return
	 */
	public List<Entry<String, String>> allHeaders() {
		// TODO make unmodifiable if committed
		return response.headers().entries();
	}
	
	public HttpVersion version() {
		return response.getProtocolVersion();
	}
	
	public JJHttpResponse content(final byte[] bytes) {
		assertNotCommitted();
		response.content().writeBytes(bytes);
		return this;
	}
	
	public JJHttpResponse content(final ByteBuf buffer) {
		assertNotCommitted();
		response.content().writeBytes(Unpooled.wrappedBuffer(buffer));
		return this;
	}
	
	public JJHttpResponse end() {
		assertNotCommitted();
		header(HttpHeaders.Names.DATE, new Date());
		maybeClose(channel.write(response));
		isCommitted = true;
		return this;
	}
	
	public void sendNotFound() {
		assertNotCommitted();
		sendError(HttpResponseStatus.NOT_FOUND);
	}
	
	public void sendError(final HttpResponseStatus status) {
		assertNotCommitted();
		byte[] body = status.reasonPhrase().getBytes(StandardCharsets.UTF_8);
		status(status)
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.header(HttpHeaders.Names.CONTENT_LENGTH, body.length)
			.header(HttpHeaders.Names.CONTENT_TYPE, "text/plain; UTF-8")
			.content(body)
			.end();
	}
	
	/**
	 * Sends a 304 Not Modified for the given resource and ends the response
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendNotModified(final Resource resource) {
		return sendNotModified(resource, false);
	}
	
	public JJHttpResponse sendNotModified(final Resource resource, boolean cache) {
		assertNotCommitted();
		
		if (cache) {
			header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR);
		}
		
		return status(HttpResponseStatus.NOT_MODIFIED)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
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
	 * asset URL and disallowing the redirect to be cached
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendTemporaryRedirect(final Resource resource) {
		assertNotCommitted();
		return status(HttpResponseStatus.TEMPORARY_REDIRECT)
			.header(HttpHeaders.Names.LOCATION, makeAbsoluteURL(resource))
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
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
	
	public JJHttpResponse sendUncachedResource(Resource resource) throws IOException {
		assertNotCommitted();
		header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE);
		if (resource instanceof TransferableResource) {
			return sendResource((TransferableResource)resource);
		} else if (resource instanceof LoadedResource) {
			return sendResource((LoadedResource)resource);
		}
		
		throw new AssertionError("trying to send a resource I don't understand");
	}
	
	public JJHttpResponse sendCachedResource(Resource resource) throws IOException {
		assertNotCommitted();
		header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR);
		if (resource instanceof TransferableResource) {
			return sendResource((TransferableResource)resource);
		} else if (resource instanceof LoadedResource) {
			return sendResource((LoadedResource)resource);
		}
		
		throw new AssertionError("trying to send a resource I don't understand");
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
	private JJHttpResponse sendResource(final LoadedResource resource) {
		return header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.CONTENT_LENGTH, resource.bytes().readableBytes())
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(resource.bytes())
			.end();
	}

	/**
	 * Transfers a resource to the connected client using the operating system
	 * zero-copy facilities.
	 * 
	 * @param resource
	 * @return
	 */
	private JJHttpResponse sendResource(TransferableResource resource) throws IOException {
		header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.header(HttpHeaders.Names.CONTENT_LENGTH, resource.size())
			.header(HttpHeaders.Names.DATE, new Date());
		
		if (resource.sha1() != null) {
			header(HttpHeaders.Names.ETAG, resource.sha1());
		}
		
		MessageList<Object> messageList = 
			MessageList.newInstance(3)
				.add(response)
				.add(new DefaultFileRegion(resource.randomAccessFile().getChannel(), 0, resource.size()))
				.add(LastHttpContent.EMPTY_LAST_CONTENT);
		
		channel.pipeline().remove(Compressor.toString());
		
		channel.write(messageList).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (HttpHeaders.isKeepAlive(request.request())) {
					channel.pipeline().addBefore(JJEngine.toString(), Compressor.toString(), new HttpContentCompressor());
				} else {
					channel.close();
				}
				log();
			}
		});
		
		isCommitted = true;
		
		return this;
	}
	
	// move all of this to another handler!
	
	private void log() {
		
		log.info(
			"request for [{}] completed in {} milliseconds (wall time)",
			request.uri(),
			request.wallTime()
		);
		
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
		// TODO check X-Forwarded-For header
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
