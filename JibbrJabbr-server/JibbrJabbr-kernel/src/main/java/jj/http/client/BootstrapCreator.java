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

import java.net.SocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author jason
 *
 */
public class BootstrapCreator {
	
	private NioEventLoopGroup eventLoopGroup;
	
	private SocketAddress localAddress;
	
	private PlaintextChannelInitializer plaintextInitializer;
	
	private SecureChannelInitializer secureInitializer;
	
	public Bootstrap plaintextHttpBootstrap() {
		
		return new Bootstrap()
			.channel(NioSocketChannel.class)
			.group(eventLoopGroup)
			.localAddress(localAddress)
			.handler(plaintextInitializer);
		
	}
	
	
}
