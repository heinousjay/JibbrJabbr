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
package jj.http.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Static responses
 * 
 * @author jason
 *
 */
public enum DefaultResponse {
	
	/** 400 Bad Request */
	BAD_REQUEST(
		new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)
	),
	
	/** 500 Internal Server Error */
	INTERNAL_SERVER_ERROR(
		new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
	),
	
	/** 501 Not Implemented */
	NOT_IMPLEMENTED(
		new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED)
	);
	
	private final DefaultFullHttpResponse response;
	
	private DefaultResponse(final DefaultFullHttpResponse response) {
		this.response = response;
	}
	
	// i mean maybe?
//	private DefaultResponse(final DefaultFullHttpResponse response, final String...headers) {
//		this.response = response;
//		assert headers.length %2 == 0;
//		for (int i = 0; i < headers.length; i += 2) {
//			response.headers().add(headers[i], headers[i + 1]);
//		}
//	}
	
	public ChannelFuture writeAndFlush(ChannelHandlerContext ctx) {
		return ctx.writeAndFlush(response);
	}
}
