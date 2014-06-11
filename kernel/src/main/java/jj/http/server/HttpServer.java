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

import jj.JJServerStartupListener;
import jj.ServerStopping;
import jj.configuration.Arguments;
import jj.configuration.Configuration;
import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.util.StringUtils;

/**
 * @author jason
 *
 */
@Singleton
@Subscriber
class HttpServer implements JJServerStartupListener {
	
	private static final ThreadFactory threadFactory = new ThreadFactory() {
		
		private final AtomicInteger id = new AtomicInteger();
		
		@Override
		public Thread newThread(Runnable r) {
			
			return new Thread(r, "JibbrJabbr HTTP Boss Handler  " + id.incrementAndGet());
		}
	};
	
	private final JJNioEventLoopGroup ioEventLoopGroup;
	
	private final HttpServerChannelInitializer initializer;
	
	private final Configuration configuration;
	
	private ServerBootstrap serverBootstrap;
	
	private final Arguments arguments;
	
	private final Publisher publisher;
	
	private final boolean run;
	
	@Inject
	HttpServer(
		final JJNioEventLoopGroup ioEventLoopGroup,
		final HttpServerChannelInitializer initializer,
		final Configuration configuration,
		final Arguments arguments,
		final Publisher publisher
	) {
		this.ioEventLoopGroup = ioEventLoopGroup;
		this.initializer = initializer;
		this.configuration = configuration;
		this.arguments = arguments;
		this.publisher = publisher;
		run = arguments.get("httpServer", boolean.class, true);
	}
	
	@Override
	public void start() throws Exception {
		
		if (run) {
		
			assert (serverBootstrap == null) : "cannot start an already started server";
			
			Binding[] bindings = getBindings();
			
			makeServerBootstrap(bindings.length);
			
			bindPorts(bindings);
			
			publisher.publish(new HttpServerStarted());
		}
	}

	private void bindPorts(Binding[] bindings) throws Exception {
		try {
			for (Binding binding : bindings) {
				
				String host = binding.host();
				int port = binding.port();
				
				if (!StringUtils.isEmpty(host)) {
					publisher.publish(new BindingHttpServer(host, port));
					serverBootstrap.bind(host, port).sync();
				} else {
					publisher.publish(new BindingHttpServer(null, port));
					serverBootstrap.bind(port).sync();
				}
			}
		} catch (Exception e) {
			serverBootstrap.group().shutdownGracefully(0, 2, SECONDS);
			throw e;
		}
	}
	
	private Binding[] getBindings() {
		
		Binding[] result;
		
		final int overridePort = arguments.get("httpPort", int.class, -1);
		if (overridePort > 1023 && overridePort < 65536) {
			result = new Binding[] { new Binding(overridePort) };
		} else {
			result = configuration.get(HttpServerSocketConfiguration.class).bindings();
		}
		
		if (result.length == 0) result = new Binding[] { new Binding(8080) };
		
		return result;
	}

	private void makeServerBootstrap(int bindingCount) {
		HttpServerSocketConfiguration config = configuration.get(HttpServerSocketConfiguration.class);
		serverBootstrap = new ServerBootstrap()
			.group(new NioEventLoopGroup(bindingCount, threadFactory), ioEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(initializer)
			.option(ChannelOption.SO_KEEPALIVE, config.keepAlive())
			.option(ChannelOption.SO_REUSEADDR, config.reuseAddress())
			.option(ChannelOption.TCP_NODELAY, config.tcpNoDelay())
			.option(ChannelOption.SO_TIMEOUT, config.timeout())
			.option(ChannelOption.SO_BACKLOG, config.backlog())
			.option(ChannelOption.SO_RCVBUF, config.receiveBufferSize())
			.option(ChannelOption.SO_SNDBUF, config.sendBufferSize());
	}
	
	@Override
	public Priority startPriority() {
		// we want to start last, everything else should be running first
		return Priority.Lowest;
	}

	@Listener
	public void stop(ServerStopping event) {
		if (run) {
			assert (serverBootstrap != null) : "cannot shut down a server that wasn't started";
			serverBootstrap.group().shutdownGracefully(1, 5, SECONDS);
			serverBootstrap.childGroup().shutdownGracefully();
			serverBootstrap = null;
			publisher.publish(new HttpServerStopped());
		}
	}

}
