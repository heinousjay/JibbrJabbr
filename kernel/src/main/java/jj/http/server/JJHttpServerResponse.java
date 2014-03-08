/*
4 *    Copyright 2012 Jason Miller
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import jj.DateFormatHelper;
import jj.Version;
import jj.http.AbstractHttpResponse;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.logging.AccessLogger;
import jj.logging.SystemLogger;
import jj.resource.Resource;
import jj.resource.TransferableResource;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedNioFile;

/**
 * @author jason
 *
 */
@Singleton
class JJHttpServerResponse extends AbstractHttpResponse {
	
	private final JJHttpServerRequest request;
	
	private final ChannelHandlerContext ctx;
	
	private final Logger access;
	
	private final SystemLogger logger;
	
	/**
	 * @param response
	 */
	@Inject
	JJHttpServerResponse(
		final Version version,
		final JJHttpServerRequest request,
		final ChannelHandlerContext ctx,
		final @AccessLogger Logger access,
		final SystemLogger logger
	) {
		this.request = request;
		this.ctx = ctx;
		this.access = access;
		this.logger = logger;
		header(HttpHeaders.Names.SERVER, String.format(
			"%s/%s (%s)",
			version.name(),
			version.version(),
			version.branchName()
		));
	}
	
	private ChannelFuture maybeClose(final ChannelFuture f) {
		if (!HttpHeaders.isKeepAlive(request.request())) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
		
		log();
		
		return f;
	}
	
	@Override
	public HttpResponse end() {
		assertNotCommitted();
		header(HttpHeaders.Names.DATE, new Date());
		ctx.write(response);
		ctx.write(content());
		maybeClose(ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT));
		markCommitted();
		return this;
	}
	
	protected String makeAbsoluteURL(final Resource resource) {
		return new StringBuilder("http")
			.append(request.secure() ? "s" : "")
			.append("://")
			.append(request.host())
			.append(resource.uri())
			.toString();
	}
	
	/**
	 * @param e
	 */
	@Override
	public HttpResponse error(Throwable e) {
		logger.error("response ended in error", e);
		sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		return this;
	}

	/**
	 * actually writes the stuff to the channel
	 * 
	 * how to test this? 
	 */
	protected HttpResponse doSendTransferableResource(TransferableResource resource) throws IOException {
		
		ctx.write(response);
		ctx.write(new ChunkedNioFile(resource.fileChannel()));
		maybeClose(ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT));
		
		// configuration can decide if we're doing zero-copy or chunking?
		//maybeClose(ctx.writeAndFlush(new DefaultFileRegion(resource.fileChannel(), 0, resource.size())));
		
		markCommitted();
		return this;
	}
	
	// move all of this to a higher-level handler
	
	private void log() {
		
		access.info(
			"request for [{}] completed in {} milliseconds (wall time) (stats events!)",
			request.uri(),
			request.wallTime()
		);
		
		if (access.isInfoEnabled()) {
			access.info("{} - - {} \"{} {} {}\" {} {} {} \"{}\"", 
				extractIP(request.remoteAddress()),
				DateFormatHelper.nowInAccessLogFormat(),
				request.method(),
				request.request().getUri(),
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
	
	private String extractReferer(final HttpRequest request) {
		
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

	/**
	 * @return {@code true} if the response has no body, {@code false} otherwise
	 */
	public boolean hasNoBody() {
		return content().readableBytes() == 0;
	}
}
