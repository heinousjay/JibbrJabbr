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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.resolver.dns.DnsNameResolverGroup;

import javax.inject.Singleton;

import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;

import com.google.inject.Inject;

/**
 * @author jason
 *
 */
@Singleton
@Subscriber
public class HttpClient {

	private final HttpClientNioEventLoopGroup eventLoop;
	private final HttpClientChannelInitializer initializer;
	private final HttpClientConfiguration configuration;
	private volatile Bootstrap bootstrap;
	
	@Inject
	HttpClient(
		final HttpClientNioEventLoopGroup eventLoop,
		final HttpClientChannelInitializer initializer,
		final HttpClientConfiguration configuration
	) throws Exception {
		this.eventLoop = eventLoop;
		this.initializer = initializer;
		this.configuration = configuration;
	}
	
	@Listener
	void configurationLoaded(ConfigurationLoaded event) {
		
		List<InetSocketAddress> nameservers = new ArrayList<>(configuration.nameservers().size());
		for (String nameserver : configuration.nameservers()) {
			try {
				nameservers.add(new InetSocketAddress(InetAddress.getByName(nameserver), 53));
			} catch (UnknownHostException uhe) {
				// publish it!
			}
		}
		
		if (nameservers.isEmpty()) {
			// publish it!
			bootstrap = null;
		} else {
		
			Bootstrap b = new Bootstrap()
				.group(eventLoop)
				.handler(initializer)
				.channel(NioSocketChannel.class)
				.resolver(new DnsNameResolverGroup(NioDatagramChannel.class, nameservers))
				.option(ChannelOption.TCP_NODELAY, true)
				.validate();
			
			bootstrap = b;
		}
	}
	
	ChannelFuture connect(String host, int port) {
		assert (bootstrap != null) : "don't call this yet!";
		// better error
		return bootstrap.connect(host, port);
	}
}
