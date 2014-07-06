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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import jj.App;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.testing.JibbrJabbrTestServer;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
@Subscriber
public class ReplIntegrationTest {
	
	@Rule
	public JibbrJabbrTestServer testServer = new JibbrJabbrTestServer(App.repl).injectInstance(this);

	Bootstrap bootstrap;
	CountDownLatch latch = new CountDownLatch(1);
	
	@Listener
	void replListening(ReplListening replListening) {
		latch.countDown();
	}
	
	@After
	public void after() {
		if (bootstrap != null) {
			bootstrap.group().shutdownGracefully(0, 0, SECONDS);
		}
	}
	
	@Test
	public void test() throws Exception {
		assertTrue("timed out", latch.await(1, SECONDS));
		
		final HashSet<String> responses = new HashSet<>(Arrays.asList("Welcome to JibbrJabbr\n>", "ReferenceError: \"whatever\" is not defined.", "\n>"));
		
		// well... it started so that's something
		// connect to 9955 and send in some commands? why not
		bootstrap = new Bootstrap()
			.group(new NioEventLoopGroup(1))
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline()
						.addLast(new StringEncoder(US_ASCII))
						.addLast(new StringDecoder(US_ASCII))
						.addLast(new SimpleChannelInboundHandler<String>() {

							@Override
							protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
								responses.remove(msg);
								latch.countDown();
							}
						});
				}
			});
		latch = new CountDownLatch(4);
		bootstrap.connect("localhost", 9955).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					future.channel().writeAndFlush("whatever\n");
				}
			}
		});
		
		assertTrue("timed out", latch.await(1, SECONDS));
		assertThat(responses, is(empty()));
	}

}
