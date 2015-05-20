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

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.application.AppLocation.Virtual;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStarting;
import jj.ServerStopping;
import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.logging.Emergency;
import jj.resource.ResourceLoader;

/**
 * starts up with the server (bound as an eager singleton)
 * 
 * listens for configuration to be loaded
 * 
 * if the repl is activated, starts the listener on the configured port or 9955 if unspecified
 * 
 * if the repl is not activated and the listener is running, stops it
 * 
 * shuts down the listener when the server goes down
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class ReplServer {

	public static final int DEFAULT_PORT = 9955;
	
	private final ReplConfiguration configuration;
	private final ReplServerChannelInitializer channelInitializer;
	private final Publisher publisher;
	private final ResourceLoader resourceLoader;
	private final UncaughtExceptionHandler uncaughtExceptionHandler;
	
	private volatile ServerBootstrap server;
	private volatile int port;
	
	@Inject
	ReplServer(
		final ReplConfiguration configuration,
		final ReplServerChannelInitializer channelInitializer,
		final Publisher publisher,
		final ResourceLoader resourceLoader,
		final UncaughtExceptionHandler uncaughtExceptionHandler
	) {
		this.configuration = configuration;
		this.channelInitializer = channelInitializer;
		this.publisher = publisher;
		this.resourceLoader = resourceLoader;
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}
	
	@Listener
	void serverStarting(ServerStarting serverStarting) {
		resourceLoader.loadResource(ReplScriptEnvironment.class, Virtual, ReplScriptEnvironment.NAME);
	}
	
	@Listener
	void serverStopping(ServerStopping serverStopping) {
		if (server != null) {
			shutdown();
		}
	}
	
	@Listener
	void configurationLoaded(ConfigurationLoaded configurationLoaded) {
		if (configuration.activate()) {
			
			if (server != null && port != configuration.port()) {
			
				server.group().terminationFuture().addListener(new GenericFutureListener<Future<Object>>() {
					@Override
					public void operationComplete(Future<Object> future) throws Exception {
						// if this failed what do we do?
						// just publish an emergency
						if (future.isSuccess()) {
							start();
						} else {
							publisher.publish(new Emergency("couldn't restart the REPL server.  The server may need to be restarted", future.cause()));
						}
					}
				});
				
				shutdown();
			
			} else if (server == null) {
				start();
			}
			
		} else if (server != null) {
			shutdown();
		}
	}
	
	
	private void start() {
		port = (configuration.port() < 1023 || configuration.port() > 65535) ? DEFAULT_PORT : configuration.port();
		
		final ServerBootstrap bootstrap = new ServerBootstrap()
			.group(
				new NioEventLoopGroup(1, executorService(uncaughtExceptionHandler, "JibbrJabbr REPL Boss ")),
				new NioEventLoopGroup(1, executorService(uncaughtExceptionHandler, "JibbrJabbr REPL Worker "))
			)
			.channel(NioServerSocketChannel.class)
			.childHandler(channelInitializer);
		
		bootstrap.bind("localhost", port).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					publisher.publish(new ReplListening(port));
					server = bootstrap;
				} else {
					publisher.publish(new Emergency("could not start the REPL server", future.cause()));
					bootstrap.group().shutdownGracefully(0, 0, SECONDS);
					bootstrap.childGroup().shutdownGracefully(0, 0, SECONDS);
				}
			} 
		});
	}
	
	private void shutdown() {
		server.group().shutdownGracefully();
		server.childGroup().shutdownGracefully();
		server = null;
		publisher.publish(new ReplStopped());
	}
	
	private static ExecutorService executorService(final UncaughtExceptionHandler uncaughtExceptionHandler, final String name) {
	
		return Executors.newFixedThreadPool(1, new ThreadFactory() {
			
			private final AtomicInteger id = new AtomicInteger();
			
			@Override
			public Thread newThread(Runnable r) {
				
				Thread thread = new Thread(r, name + id.incrementAndGet());
				thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
				return thread;
			}
		});
	}

}
