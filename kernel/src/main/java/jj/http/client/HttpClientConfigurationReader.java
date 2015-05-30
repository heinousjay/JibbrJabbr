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

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Helper to externalize logic around validating/transforming address configurations
 * into actual socket addresses.  Makes testing easier and separates concerns
 */
@Singleton
class HttpClientConfigurationReader {
	
	private final HttpClientConfiguration configuration;
	
	@Inject
	HttpClientConfigurationReader(final HttpClientConfiguration configuration) {
		this.configuration = configuration;
	}
	
	InetSocketAddress localClientAddress() {
		InetSocketAddress localAddress = new InetSocketAddress((InetAddress)null, 0);
		String ip = configuration.localClientAddress();
		if (ip != null) {
			try {
				localAddress = new InetSocketAddress(InetAddress.getByName(ip), 0);
			} catch (UnknownHostException uhe) {
				// publish it!
			}
		}
		
		return localAddress;
	}
	
	InetSocketAddress localNameserverAddress() {
		InetSocketAddress localAddress = new InetSocketAddress((InetAddress)null, 0);
		String ip = configuration.localNameserverAddress();
		if (ip != null) {
			try {
				localAddress = new InetSocketAddress(InetAddress.getByName(ip), 0);
			} catch (UnknownHostException uhe) {
				// publish it!
			}
		}
		
		return localAddress;
	}

	List<InetSocketAddress> nameservers() {
		List<InetSocketAddress> nameservers = new ArrayList<>(configuration.nameservers().size());
		for (String nameserver : configuration.nameservers()) {
			try {
				nameservers.add(new InetSocketAddress(InetAddress.getByName(nameserver), 53));
			} catch (UnknownHostException uhe) {
				// publish it!
			}
		}
		return nameservers;
	}
	
	@Override
	public int hashCode() {
		return configuration.hashCode();
	}
}