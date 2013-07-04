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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJServerListener;
import jj.configuration.Configuration;

/**
 * @author jason
 *
 */
@Singleton
class HttpServer implements JJServerListener {
	
	private static final ThreadFactory threadFactory = new ThreadFactory() {
		
		private final AtomicInteger id = new AtomicInteger();
		
		@Override
		public Thread newThread(Runnable r) {
			
			return new Thread(r, "JibbrJabbr HTTP Boss Handler  " + id.incrementAndGet());
		}
	};
	
	private final Logger logger = LoggerFactory.getLogger(HttpServer.class);
	
	private final JJNioEventLoopGroup ioEventLoopGroup;
	
	private final HttpServerChannelInitializer initializer;
	
	private ServerBootstrap serverBootstrap;
	
	@Inject
	HttpServer(
		final JJNioEventLoopGroup ioEventLoopGroup,
		final HttpServerChannelInitializer initializer,
		final Configuration configuration
	) {
		this.ioEventLoopGroup = ioEventLoopGroup;
		this.initializer = initializer;
	}
	
	private ServerBootstrap serverBootstrap() {
		return new ServerBootstrap()
			.group(new NioEventLoopGroup(1, threadFactory), ioEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(initializer);
	}
	
	@Override
	public void start() throws Exception {
		assert (serverBootstrap == null) : "cannot start an already started server";
		serverBootstrap = serverBootstrap();
		serverBootstrap.bind(8080).sync();
		logger.info("Server started");
	}

	@Override
	public void stop() {
		assert (serverBootstrap != null) : "cannot shut down a server that wasn't started";
		serverBootstrap.group().shutdownGracefully();
		serverBootstrap.childGroup().shutdownGracefully();
		serverBootstrap = null;
		logger.info("Server shut down");
	}

}
