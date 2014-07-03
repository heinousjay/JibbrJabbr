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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStopping;
import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.logging.Emergency;

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
	private volatile ServerBootstrap server;
	private volatile int port;
	
	@Inject
	ReplServer(
		final ReplConfiguration configuration,
		final ReplServerChannelInitializer channelInitializer,
		final Publisher publisher
	) {
		this.configuration = configuration;
		this.channelInitializer = channelInitializer;
		this.publisher = publisher;
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
						start();
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
			.group(new NioEventLoopGroup(1), new NioEventLoopGroup(1))
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
	}

}
