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

import static org.mockito.BDDMockito.*;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import javax.inject.Provider;

import jj.AnswerWithSelf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ReplServerChannelInitializerTest {
	
	@Mock SocketChannel ch;
	
	Provider<ReplHandler> replHandlerProvider = new Provider<ReplHandler>() {

		@Override
		public ReplHandler get() {
			return mock(ReplHandler.class);
		}
		
	};

	@Test
	public void test() throws Exception {
		ChannelPipeline pipeline = mock(ChannelPipeline.class, new AnswerWithSelf());
		given(ch.pipeline()).willReturn(pipeline);
		
		ReplServerChannelInitializer rsci = new ReplServerChannelInitializer(replHandlerProvider);
		
		rsci.initChannel(ch);
		
		InOrder inOrder = inOrder(pipeline);
		
		inOrder.verify(ch.pipeline()).addLast(isA(DelimiterBasedFrameDecoder.class));
		inOrder.verify(ch.pipeline()).addLast(isA(StringDecoder.class));
		inOrder.verify(ch.pipeline()).addLast(isA(StringEncoder.class));
		inOrder.verify(ch.pipeline()).addLast(isA(ReplHandler.class));
	}

}
