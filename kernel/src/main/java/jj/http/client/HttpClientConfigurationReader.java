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

import io.netty.resolver.dns.DnsServerAddresses;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Helper to externalize logic around validating/transforming address configurations
 * into actual socket addresses.  Makes testing easier and separates concerns
 */
@Singleton
class HttpClientConfigurationReader {

	private static final InetSocketAddress DEFAULT_LOCAL_ADDRESS = new InetSocketAddress((InetAddress)null, 0);
	
	private final HttpClientConfiguration configuration;
	
	@Inject
	HttpClientConfigurationReader(final HttpClientConfiguration configuration) {
		this.configuration = configuration;
	}

	private InetSocketAddress newInetSocketAddress(String nameserverAddress) {
		if (nameserverAddress == null || nameserverAddress.isEmpty()) {
			return null;
		}

		try {
			return new InetSocketAddress(InetAddress.getByName(nameserverAddress), 53);
		} catch (UnknownHostException uhe) {
			// TODO publish it!
			return null;
		}
	}
	
	InetSocketAddress localClientAddress() {
		InetSocketAddress localAddress = newInetSocketAddress(configuration.localClientAddress());
		return localAddress != null ? localAddress : DEFAULT_LOCAL_ADDRESS;
	}
	
	InetSocketAddress localNameserverAddress() {
		InetSocketAddress localAddress = newInetSocketAddress(configuration.localNameserverAddress());
		return localAddress != null ? localAddress : DEFAULT_LOCAL_ADDRESS;
	}

	private  DnsServerAddresses useDefaultAddresses() {
		// TODO publish it!
		return DnsServerAddresses.defaultAddresses();
	}

	DnsServerAddresses nameservers() {
		List<InetSocketAddress> nameservers = configuration.nameservers().stream()
			.map(this::newInetSocketAddress)
			.filter(o -> o != null)
			.collect(Collectors.toList());

		return nameservers.isEmpty() ?
				useDefaultAddresses() :
				DnsServerAddresses.sequential(nameservers);
	}
	
	@Override
	public int hashCode() {
		return configuration.hashCode();
	}
}