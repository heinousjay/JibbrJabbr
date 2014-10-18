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

import java.util.Arrays;
import java.util.List;
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
	
	/**
	 * 
	 */
	private static final int DEFAULT_BINDING_PORT = 8080;

	private static final ThreadFactory threadFactory = new ThreadFactory() {
		
		private final AtomicInteger id = new AtomicInteger();
		
		@Override
		public Thread newThread(Runnable r) {
			
			return new Thread(r, "JibbrJabbr HTTP Boss Handler  " + id.incrementAndGet());
		}
	};
	
	private final HttpServerNioEventLoopGroup ioEventLoopGroup;
	
	private final HttpServerChannelInitializer initializer;
	
	private final HttpServerSocketConfiguration configuration;
	
	private ServerBootstrap serverBootstrap;
	
	private final HttpServerSwitch httpServerSwitch;
	
	private final Publisher publisher;
	
	@Inject
	HttpServer(
		final HttpServerNioEventLoopGroup ioEventLoopGroup,
		final HttpServerChannelInitializer initializer,
		final HttpServerSocketConfiguration configuration,
		final HttpServerSwitch httpServerSwitch,
		final Publisher publisher
	) {
		this.ioEventLoopGroup = ioEventLoopGroup;
		this.initializer = initializer;
		this.configuration = configuration;
		this.httpServerSwitch = httpServerSwitch;
		this.publisher = publisher;
	}
	
	@Override
	public void start() throws Exception {
		
		if (httpServerSwitch.on()) {
		
			assert (serverBootstrap == null) : "cannot start an already started server";
			
			List<Binding> bindings = getBindings();
			
			makeServerBootstrap(bindings.size());
			
			bindPorts(bindings);
			
			publisher.publish(new HttpServerStarted());
		}
	}

	private void bindPorts(List<Binding> bindings) throws Exception {
		try {
			for (Binding binding : bindings) {
				
				String host = binding.host();
				int port = binding.port();
				
				if (!StringUtils.isEmpty(host)) {
					
					serverBootstrap.bind(host, port).sync();
				} else {
					serverBootstrap.bind(port).sync();
				}
				publisher.publish(new BindingHttpServer(binding));
			}
		} catch (Exception e) {
			serverBootstrap.group().shutdownGracefully(0, 2, SECONDS);
			throw e;
		}
	}
	
	private List<Binding> getBindings() {
		
		List<Binding> result;
		
		final int overridePort = httpServerSwitch.port();
		if (overridePort > 1023 && overridePort < 65536) {
			result = Arrays.asList(new Binding(overridePort));
		} else {
			result = configuration.bindings();
		}
		
		if (result.isEmpty()) result = Arrays.asList(new Binding(DEFAULT_BINDING_PORT));
		
		return result;
	}

	private void makeServerBootstrap(int bindingCount) {
		serverBootstrap = new ServerBootstrap()
			.group(new NioEventLoopGroup(bindingCount, threadFactory), ioEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(initializer)
			.option(ChannelOption.SO_KEEPALIVE, configuration.keepAlive())
			.option(ChannelOption.SO_REUSEADDR, configuration.reuseAddress())
			.option(ChannelOption.TCP_NODELAY, configuration.tcpNoDelay())
			.option(ChannelOption.SO_TIMEOUT, configuration.timeout())
			.option(ChannelOption.SO_BACKLOG, configuration.backlog())
			.option(ChannelOption.SO_RCVBUF, configuration.receiveBufferSize())
			.option(ChannelOption.SO_SNDBUF, configuration.sendBufferSize());
	}
	
	@Override
	public Priority startPriority() {
		// we want to start last, everything else should be running first
		return Priority.Lowest;
	}

	@Listener
	public void stop(ServerStopping event) {
		if (httpServerSwitch.on()) {
			assert (serverBootstrap != null) : "cannot shut down a server that wasn't started";
			serverBootstrap.group().shutdownGracefully(1, 5, SECONDS);
			serverBootstrap.childGroup().shutdownGracefully();
			serverBootstrap = null;
			publisher.publish(new HttpServerStopped());
		}
	}

}
