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

import javax.inject.Singleton;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;

/**
 * @author jason
 *
 */
@Singleton
@Sharable
class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	static final String CODEC = "codec";

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		ch.pipeline()
			.addLast(new HttpClientCodec())
			.addLast(new HttpContentDecompressor());
	}

}
