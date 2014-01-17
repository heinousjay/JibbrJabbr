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

import static jj.http.server.PipelineStages.*;
import static org.mockito.BDDMockito.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.inject.Provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpServerChannelInitializerTest {

	
	@Mock Provider<JJEngineHttpHandler> engineProvider;
	
	@InjectMocks HttpServerChannelInitializer hsci;
	
	@Mock JJEngineHttpHandler engine;
	
	// mocking things we don't own because said things
	// connect to a network and they're just interfaces 
	@Mock SocketChannel ch;
	@Mock ChannelPipeline pipeline; 
	
	@Test
	public void test() throws Exception {
		
		// given
		given(engineProvider.get()).willReturn(engine);
		given(ch.pipeline()).willReturn(pipeline);
		given(pipeline.addLast(anyString(), any(ChannelHandler.class))).willReturn(pipeline);
		
		
		// when
		hsci.initChannel(ch);
		
		
		// then
		InOrder i = inOrder(pipeline);
		
		i.verify(pipeline).addLast(eq(Decoder.toString()), isA(HttpRequestDecoder.class));
		i.verify(pipeline).addLast(eq(Aggregator.toString()), isA(HttpObjectAggregator.class));
		i.verify(pipeline).addLast(eq(Encoder.toString()), isA(HttpResponseEncoder.class));
		i.verify(pipeline).addLast(eq(ChunkedWriter.toString()), isA(ChunkedWriteHandler.class));
		i.verify(pipeline).addLast(JJEngine.toString(), engine);
		
		// this test acts as an inventory of the handlers, so
		// ensure nothing else happened
		verifyNoMoreInteractions(pipeline);
	}

}
