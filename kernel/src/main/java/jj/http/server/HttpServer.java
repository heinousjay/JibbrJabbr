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

import static java.util.concurrent.TimeUnit.*;


import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;


import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;


import jj.ServerStopping;
import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.execution.TaskRunner;
import jj.logging.Emergency;
import jj.util.StringUtils;

/**
 * <p>
 * Manages the netty ServerBootstrap instance for HTTP serving.
 * 
 * <p>
 * Mainly, this class acts as an interface point to the configuration system,
 * it listens for 
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class HttpServer {

	private static ExecutorService executorService(final int threads, final UncaughtExceptionHandler uncaughtExceptionHandler) {
	
		return Executors.newFixedThreadPool(threads, new ThreadFactory() {
			
			private final AtomicInteger id = new AtomicInteger();
			
			@Override
			public Thread newThread(Runnable r) {
				
				Thread thread = new Thread(r, "JibbrJabbr HTTP Boss Handler  " + id.incrementAndGet());
				thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
				return thread;
			}
		});
	}
	
	private final HttpServerNioEventLoopGroup ioEventLoopGroup;
	
	private final HttpServerChannelInitializer initializer;
	
	private final HttpServerSocketConfiguration configuration;
	
	private final HttpServerSwitch httpServerSwitch;
	
	private final Publisher publisher;
	
	private final TaskRunner taskRunner;
	
	private final Provider<ServerBootstrap> serverBootstrapProvider;
	
	private final UncaughtExceptionHandler uncaughtExceptionHandler;
	
	private ServerBootstrap serverBootstrap;
	
	private volatile int configurationHashCode;
	
	@Inject
	HttpServer(
		final HttpServerNioEventLoopGroup ioEventLoopGroup,
		final HttpServerChannelInitializer initializer,
		final HttpServerSocketConfiguration configuration,
		final HttpServerSwitch httpServerSwitch,
		final Publisher publisher,
		final TaskRunner taskRunner,
		final Provider<ServerBootstrap> serverBootstrapProvider,
		final UncaughtExceptionHandler uncaughtExceptionHandler
	) {
		this.ioEventLoopGroup = ioEventLoopGroup;
		this.initializer = initializer;
		this.configuration = configuration;
		this.httpServerSwitch = httpServerSwitch;
		this.publisher = publisher;
		this.taskRunner = taskRunner;
		this.serverBootstrapProvider = serverBootstrapProvider;
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}
	
	@Listener
	void on(ConfigurationLoaded event) {
		if (httpServerSwitch.on()) {
			checkStart();
		}
	}

	@Listener
	void on(ServerStopping event) {
		if (httpServerSwitch.on() && serverBootstrap != null) {
			stop(serverBootstrap);
			publisher.publish(new HttpServerStopped());
			serverBootstrap = null;
		}
	}
	
	private void checkStart() {
		if (configurationHashCode != configuration.hashCode()) {
			configurationHashCode = configuration.hashCode();
			taskRunner.execute(new HttpServerTask("starting the http server") {
				
				@Override
				protected void run() throws Exception {
					if (serverBootstrap != null) {
						stop(serverBootstrap).addListener((future) -> {
							serverBootstrap = null;
							if (future.isSuccess()) {
								publisher.publish(new HttpServerRestarting());
								start();
							} else {
								publisher.publish(new Emergency("could not restart the HTTP server", future.cause()));
							}
						});
					} else {
						start();
					}
				}
			});
		}
	}
	
	private Future<?> stop(ServerBootstrap serverBootstrap) {
		return serverBootstrap.group().shutdownGracefully(0, 250, MILLISECONDS).addListener((future) -> {
			if (!future.isSuccess()) {} // uhhhh - no clue here really. can't imagine the failure mode yet
			if (serverBootstrap.childGroup() != null) {
				serverBootstrap.childGroup().shutdownGracefully(); // don't really care about waiting for these
			}
		});
	}

	private void start() throws Exception {
		List<Binding> bindings = configuration.bindings();
		if (!bindings.isEmpty()) {
			serverBootstrap = bindPorts(makeServerBootstrap(bindings.size()), bindings);
			publisher.publish(new HttpServerStarted());
		} else {
			serverBootstrap = null;
		}
	}

	private ServerBootstrap makeServerBootstrap(int bindingCount) {
		return serverBootstrapProvider.get()
			.group(new NioEventLoopGroup(bindingCount, executorService(bindingCount, uncaughtExceptionHandler)), ioEventLoopGroup)
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

	private ServerBootstrap bindPorts(ServerBootstrap serverBootstrap, List<Binding> bindings) throws Exception {
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
			
			return serverBootstrap;
		} catch (Exception e) {
			e.printStackTrace();
			stop(serverBootstrap);
			throw e;
		}
	}

}
