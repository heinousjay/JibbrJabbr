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
package jj.http.client;

import java.net.InetSocketAddress;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.resolver.dns.DnsNameResolverGroup;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.configuration.ConfigurationLoaded;
import jj.configuration.ConfigurationLoading;
import jj.event.Listener;
import jj.event.Subscriber;

/**
 * @author jason
 *
 */
@Singleton
@Subscriber
class HttpClient {

	private final HttpClientNioEventLoopGroup eventLoop;
	private final HttpClientChannelInitializer initializer;
	private final HttpClientConfigurationReader configuration;
	private final Provider<Bootstrap> bootstrapProvider;
	private volatile Bootstrap bootstrap;
	
	// we can store this to verify that the configuration is the same
	// and not restart
	// no need to be volatile as it will only ever be used from the
	// configuration script thread
	private int configurationHashCode;
	
	@Inject
	HttpClient(
		final HttpClientNioEventLoopGroup eventLoop,
		final HttpClientChannelInitializer initializer,
		final HttpClientConfigurationReader configuration,
		final Provider<Bootstrap> bootstrapProvider
	) {
		this.eventLoop = eventLoop;
		this.initializer = initializer;
		this.configuration = configuration;
		this.bootstrapProvider = bootstrapProvider;
	}
	
	@Listener
	void configurationLoading(ConfigurationLoading event) {
		makeBootstrap();
	}
	
	@Listener
	void configurationLoaded(ConfigurationLoaded event) {
		makeBootstrap();
	}
	
	private void makeBootstrap() {
		
		if (configuration.hashCode() != configurationHashCode) {
			configurationHashCode = configuration.hashCode();
			
			List<InetSocketAddress> nameservers = configuration.nameservers();
			
			if (nameservers.isEmpty()) {
				// publish it!
				bootstrap = null;
			} else {
			
				Bootstrap b = bootstrapProvider.get()
					.group(eventLoop)
					.handler(initializer)
					.channel(NioSocketChannel.class)
					.localAddress(configuration.localClientAddress())
					.resolver(new DnsNameResolverGroup(NioDatagramChannel.class, configuration.localNameserverAddress(), nameservers))
					.option(ChannelOption.TCP_NODELAY, true)
					.validate();
				
				bootstrap = b;
			}
		}
	}
	
	ChannelFuture connect(boolean secure, String host, int port) {
		assert (bootstrap != null) : "don't call this yet!";
		// better error
		return bootstrap.connect(host, port);
	}
}
