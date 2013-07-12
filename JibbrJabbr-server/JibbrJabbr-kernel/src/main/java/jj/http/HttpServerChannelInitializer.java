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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * @author jason
 * 
 */
@Singleton
class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	enum PipelineStages {
		Decoder,
		Aggregator,
		Encoder,
		ChunkedWriter,
		JJEngine,
		JJWebsocketHandler
	}
	
	private final Provider<JJEngineHttpHandler> engineProvider;
	
	@Inject
	public HttpServerChannelInitializer(final Provider<JJEngineHttpHandler> engineProvider) {
		this.engineProvider = engineProvider;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast(Decoder.toString(), new HttpRequestDecoder())
			.addLast(Aggregator.toString(), new HttpObjectAggregator(8192))
			.addLast(Encoder.toString(), new HttpResponseEncoder())
			.addLast(ChunkedWriter.toString(), new ChunkedWriteHandler())
			.addLast(JJEngine.toString(), engineProvider.get());
	}

}
