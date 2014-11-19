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
package jj.http.server.methods;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Implementation of an http method to be processed by
 * the server.  
 * 
 * @author jason
 *
 */
public abstract class HttpMethodHandler extends SimpleChannelInboundHandler<Object> {
	
	private HttpRequest request;
	
	public HttpMethodHandler request(final HttpRequest request) {
		this.request = request;
		return this;
	}

	@Override
	protected final void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		assert request != null : "request was not set!";
		
		if (msg == request) {
			handleHttpRequest(ctx, request);
		} else if (msg instanceof HttpContent) {
			handleHttpContent(ctx, (HttpContent)msg);
		} else {
			throw new AssertionError("pipeline error, received an unacceptable message " + msg);
		}
	}

	abstract protected void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request);
	
	abstract protected void handleHttpContent(ChannelHandlerContext ctx, HttpContent content);
}
