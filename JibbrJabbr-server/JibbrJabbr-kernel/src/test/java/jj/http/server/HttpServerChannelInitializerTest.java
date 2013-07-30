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

import static jj.http.server.HttpServerChannelInitializer.PipelineStages.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpServerChannelInitializerTest {

	HttpServerChannelInitializer hsci;
	@Mock Provider<JJEngineHttpHandler> engineProvider;
	@Mock JJEngineHttpHandler engine;
	@Mock SocketChannel ch;
	@Mock ChannelPipeline pipeline;
	@Captor ArgumentCaptor<ChannelHandler> handlerCaptor; 
	
	@Before
	public void before() {
		
		given(engineProvider.get()).willReturn(engine);
		given(ch.pipeline()).willReturn(pipeline);
		given(pipeline.addLast(anyString(), any(ChannelHandler.class))).willReturn(pipeline);
		
		hsci = new HttpServerChannelInitializer(engineProvider);
	}
	
	@Test
	public void test() throws Exception {
		hsci.initChannel(ch);
		
		verify(pipeline).addLast(eq(Decoder.toString()), handlerCaptor.capture());
		assertTrue(handlerCaptor.getValue() instanceof HttpRequestDecoder);
		
		verify(pipeline).addLast(eq(Aggregator.toString()), handlerCaptor.capture());
		assertTrue(handlerCaptor.getValue() instanceof HttpObjectAggregator);
		
		verify(pipeline).addLast(eq(Encoder.toString()), handlerCaptor.capture());
		assertTrue(handlerCaptor.getValue() instanceof HttpResponseEncoder);
		
		verify(pipeline).addLast(eq(ChunkedWriter.toString()), handlerCaptor.capture());
		assertTrue(handlerCaptor.getValue() instanceof ChunkedWriteHandler);
		
		verify(pipeline).addLast(eq(JJEngine.toString()), handlerCaptor.capture());
		assertThat(engine, is(sameInstance(handlerCaptor.getValue())));
		
		// this test acts as an inventory of the handlers
		verifyNoMoreInteractions(pipeline);
	}

}
