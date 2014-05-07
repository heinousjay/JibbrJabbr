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
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Version;
import jj.event.Publisher;
import jj.http.AbstractHttpResponse;
import jj.http.HttpResponse;
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
	
	private final Publisher publisher;
	
	/**
	 * @param response
	 */
	@Inject
	JJHttpServerResponse(
		final Version version,
		final JJHttpServerRequest request,
		final ChannelHandlerContext ctx,
		final Publisher publisher
	) {
		this.request = request;
		this.ctx = ctx;
		this.publisher = publisher;
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
		
		publisher.publish(new RequestResponded(request, this));
		
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
	
	@Override
	public HttpResponse error(Throwable t) {
		publisher.publish(new RequestErrored(t));
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
		// can i even make zero-copy work again? haha
		//maybeClose(ctx.writeAndFlush(new DefaultFileRegion(resource.fileChannel(), 0, resource.size())));
		
		markCommitted();
		return this;
	}

	/**
	 * @return {@code true} if the response has no body, {@code false} otherwise
	 */
	public boolean hasNoBody() {
		return content().readableBytes() == 0;
	}
}
