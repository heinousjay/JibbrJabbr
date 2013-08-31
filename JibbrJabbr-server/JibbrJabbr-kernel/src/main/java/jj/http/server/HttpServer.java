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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJServerShutdownListener;
import jj.JJServerStartupListener;
import jj.StringUtils;
import jj.configuration.Configuration;
import jj.execution.JJNioEventLoopGroup;

/**
 * @author jason
 *
 */
@Singleton
class HttpServer implements JJServerStartupListener, JJServerShutdownListener {
	
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
	
	private final Configuration configuration;
	
	private ServerBootstrap serverBootstrap;
	
	@Inject
	HttpServer(
		final JJNioEventLoopGroup ioEventLoopGroup,
		final HttpServerChannelInitializer initializer,
		final Configuration configuration
	) {
		this.ioEventLoopGroup = ioEventLoopGroup;
		this.initializer = initializer;
		this.configuration = configuration;
	}
	
	@Override
	public void start() throws Exception {
		
		assert (serverBootstrap == null) : "cannot start an already started server";
		
		HttpServerSocketConfiguration config = configuration.get(HttpServerSocketConfiguration.class);
		
		Binding[] bindings = config.bindings();
		if (bindings.length == 0) bindings = new Binding[] {
			new Binding() {
				
				@Override
				public int port() {
					return 8080;
				}
				
				@Override
				public String host() {
					return null;
				}
			}
		};
		
		serverBootstrap =  new ServerBootstrap()
			.group(new NioEventLoopGroup(bindings.length, threadFactory), ioEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(initializer)
			.option(ChannelOption.SO_KEEPALIVE, config.keepAlive())
			.option(ChannelOption.SO_REUSEADDR, config.reuseAddress())
			.option(ChannelOption.TCP_NODELAY, config.tcpNoDelay())
			.option(ChannelOption.SO_TIMEOUT, config.timeout())
			.option(ChannelOption.SO_BACKLOG, config.backlog())
			.option(ChannelOption.SO_RCVBUF, config.receiveBufferSize())
			.option(ChannelOption.SO_SNDBUF, config.sendBufferSize());
		
		try {
			for (Binding binding : bindings) {
				
				String host = binding.host();
				int port = binding.port();
				
				if (!StringUtils.isEmpty(host)) {
					logger.info("Binding to {}:{}", host, port);
					serverBootstrap.bind(host, port).sync();
				} else {
					logger.info("Binding to {}", port);
					serverBootstrap.bind(port).sync();
				}
			}
		} catch (Exception e) {
			serverBootstrap.group().shutdownGracefully(0, 2, SECONDS);
			throw e;
		}
		
		logger.info("Server started");
	}
	
	@Override
	public Priority startPriority() {
		// we want to start last, everything else should be running first
		return Priority.Lowest;
	}

	@Override
	public void stop() {
		assert (serverBootstrap != null) : "cannot shut down a server that wasn't started";
		serverBootstrap.group().shutdownGracefully(1, 5, SECONDS);
		serverBootstrap.childGroup().shutdownGracefully();
		serverBootstrap = null;
		logger.info("Server shut down");
	}

}
