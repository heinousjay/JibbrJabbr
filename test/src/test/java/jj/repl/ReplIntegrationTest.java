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
import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.*;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

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
	
	@Inject ReplConfiguration config;

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
	public void test() throws Throwable {
		assertTrue("timed out waiting for init", latch.await(500, MILLISECONDS));
		
		// well... it started so that's something
		// connect to config.port() and send in some commands? why not
		latch = new CountDownLatch(1);
		
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		
		final StringBuilder response = new StringBuilder()
			.append("Welcome to JibbrJabbr\n>")
			.append("ReferenceError: \"whatever\" is not defined.\n")
			.append("	at repl-console:1\n")
			.append("	at base-repl-system.js:8 ($$print)\n")
			.append("	at repl-console:1\n")
			.append("\n>");
		
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
								if (msg.equals(response.substring(0, msg.length()))) {
									response.delete(0, msg.length());
								}
								if (response.length() == 0) {
									latch.countDown();
								}
							}
						});
				}
			});
		bootstrap.connect("localhost", config.port()).addListener((ChannelFuture future) -> {
			if (future.isSuccess()) {
				future.channel().writeAndFlush("whatever\n");
			} else {
				failure.set(future.cause());
			}
		});
		
		assertTrue("timed out waiting for response", latch.await(1, SECONDS));
		if (failure.get() != null) {
			throw failure.get();
		}
	}

}
