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

import static jj.AnswerWithSelf.ANSWER_WITH_SELF;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.resolver.dns.DnsAddressResolverGroup;

import javax.inject.Provider;

import jj.configuration.ConfigurationLoading;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpClientTest {
	
	@Mock HttpClientNioEventLoopGroup group;
	@Mock HttpClientChannelInitializer initializer;
	@Mock HttpClientConfigurationReader configuration;
	Bootstrap bootstrap;
	Provider<Bootstrap> provider = new Provider<Bootstrap>() {
		public Bootstrap get() {
			return bootstrap;
		}
	};
	
	HttpClient client;
	
	@Captor ArgumentCaptor<SocketAddress> localAddressCaptor;
	@Captor ArgumentCaptor<DnsAddressResolverGroup> resolverCaptor;
	
	@Mock InetSocketAddress localClientAddress;
	@Mock InetSocketAddress localNameserverAddress;
	InetSocketAddress nameserverAddress1 = new InetSocketAddress("localhost", 8080);
	InetSocketAddress nameserverAddress2 = new InetSocketAddress("localhost", 8081);
	
	@Before
	public void before() {
		bootstrap = mock(Bootstrap.class, ANSWER_WITH_SELF);
		client = new HttpClient(group, initializer, configuration, provider);
	}

	@Test
	public void test() {
		
		given(configuration.localClientAddress()).willReturn(localClientAddress);
		given(configuration.localNameserverAddress()).willReturn(localNameserverAddress);
		given(configuration.nameservers()).willReturn(Arrays.asList(nameserverAddress1, nameserverAddress2));
		
		client.on((ConfigurationLoading)null);
		
		verify(bootstrap).group(group);
		verify(bootstrap).handler(initializer);
		verify(bootstrap).channel(NioSocketChannel.class);
		verify(bootstrap).localAddress(localAddressCaptor.capture());
		
		// prevents NPE to same-instance this
		assertThat(localAddressCaptor.getValue(), is(sameInstance(localClientAddress)));
		
		verify(bootstrap).resolver(resolverCaptor.capture());
		// might need to reflect into it!
		// and the options!
		
		// make sure we try to catch errors
		verify(bootstrap).validate();
	}
}
