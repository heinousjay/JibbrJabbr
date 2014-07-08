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
package jj.repl;

import static java.nio.charset.StandardCharsets.US_ASCII;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author jason
 *
 */
@Singleton
public class ReplServerChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final Provider<ReplHandler> replHandlerProvider;
	
	@Inject
	ReplServerChannelInitializer(
		final Provider<ReplHandler> replHandlerProvider
	) {
		this.replHandlerProvider = replHandlerProvider;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		
		p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
			.addLast(new StringDecoder(US_ASCII))
			.addLast(new StringEncoder(US_ASCII))
			.addLast(replHandlerProvider.get());
	}

}
