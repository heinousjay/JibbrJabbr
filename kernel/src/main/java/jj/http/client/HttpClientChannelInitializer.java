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

import static java.nio.charset.StandardCharsets.*;

import javax.inject.Singleton;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author jason
 *
 */
@Singleton
@Sharable
class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		ch.pipeline()
			.addLast(new HttpClientCodec())
			.addLast(new SimpleChannelInboundHandler<HttpResponse>() {

				@Override
				protected void messageReceived(ChannelHandlerContext ctx, HttpResponse msg) throws Exception {
					System.out.println(msg);
				}
			})
			.addLast(new SimpleChannelInboundHandler<HttpContent> () {

				@Override
				protected void messageReceived(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
					System.out.println(msg.content().toString(UTF_8));
				}
				
			});
	}

}
