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
package jj.http.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * 
 * @author jason
 *
 */
public abstract class HttpResponseListener {

	ChannelHandlerAdapter handler() {
		return handler;
	}

	private ChannelHandlerAdapter handler = new ChannelInboundHandlerAdapter() {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof HttpResponse) {

				HttpResponse response = (HttpResponse)msg;
				responseStart(response);

			} else if (msg instanceof LastHttpContent) {

				LastHttpContent content = (LastHttpContent)msg;
				bodyPart(content.content());
				responseComplete(content.trailingHeaders());
				// temporary! until keep-alive is in here
				ctx.close();

			} else if (msg instanceof HttpContent) {

				bodyPart(((HttpContent)msg).content());

			} else {
				throw new AssertionError("UNEXPECTED RESPONSE OBJECT " + msg.getClass());
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			requestErrored(cause);
		}
	};

	/**
	 * Called when the initial line and headers of the response have been received
	 * @param response
	 */
	protected void responseStart(HttpResponse response) {}

	protected void bodyPart(ByteBuf bodyPart) {}

	protected void responseComplete(HttpHeaders trailingHeaders) {}
	
	protected void requestErrored(Throwable cause) {}
}
