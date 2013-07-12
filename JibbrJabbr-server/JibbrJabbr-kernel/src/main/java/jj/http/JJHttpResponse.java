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
package jj.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.DateFormatHelper;
import jj.Version;
import jj.execution.ExecutionTrace;
import jj.logging.AccessLogger;
import jj.resource.Resource;
import jj.resource.TransferableResource;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.MessageList;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * @author jason
 *
 */
@Singleton
class JJHttpResponse extends AbstractHttpResponse {
	
	private static final String SERVER_NAME = String.format(
		"%s/%s (%s)",
		Version.name,
		Version.version,
		Version.branchName		
	);

	private static final Logger log = LoggerFactory.getLogger(JJHttpResponse.class);
	
	private final JJHttpRequest request;
	
	private final Channel channel;
	
	private final Logger access;
	
	private final ExecutionTrace trace;
	
	/**
	 * @param response
	 */
	@Inject
	JJHttpResponse(
		final JJHttpRequest request,
		final Channel channel,
		final @AccessLogger Logger access,
		final ExecutionTrace trace
	) {
		this.request = request;
		this.channel = channel;
		this.access = access;
		this.trace = trace;
		header(HttpHeaders.Names.SERVER, SERVER_NAME);
	}
	
	private void maybeClose(final ChannelFuture f) {
		if (!HttpHeaders.isKeepAlive(request.request())) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
		
		log();
	}
	
	@Override
	public HttpResponse end() {
		assertNotCommitted();
		header(HttpHeaders.Names.DATE, new Date());
		maybeClose(channel.write(response));
		markCommitted();
		trace.end(request, this);
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
		log.error("response ended in error", e);
		sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		return this;
	}

	/**
	 * actually writes the stuff to the channel
	 * 
	 * how to test this? 
	 */
	protected HttpResponse doSendTransferableResource(TransferableResource resource) throws IOException {
		MessageList<Object> messageList = 
			MessageList.newInstance(3)
				.add(response)
				.add(new DefaultFileRegion(resource.fileChannel(), 0, resource.size()))
				.add(LastHttpContent.EMPTY_LAST_CONTENT);
		
		maybeClose(channel.write(messageList));
		markCommitted();
		trace.end(request, this);
		return this;
	}
	
	// move all of this to a higher-level handler
	
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
		// TODO Auto-generated method stub
		return response.content().readableBytes() == 0;
	}
}
