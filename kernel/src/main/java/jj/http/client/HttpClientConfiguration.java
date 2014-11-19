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

import java.util.List;

import jj.configuration.Default;
import jj.configuration.DefaultProvider;

/**
 * @author jason
 *
 */
public interface HttpClientConfiguration {
	
	/**
	 * the ip address of the local interface to use for http client communication
	 * if left null, will use the wildcard (all local interfaces)
	 * if the string "loopback" will use the loopback address
	 * otherwise it has to be a valid ipv4 or ipv6 address
	 */
	String localClientAddress();
	
	/**
	 * the ip address of the local interface to use for name resolution
	 * if left null, will use the wildcard (all local interfaces)
	 * if the string "loopback" will use the loopback address
	 * otherwise it has to be a valid ipv4 or ipv6 address
	 */
	String localNameserverAddress();
	
	/**
	 * 1 or more nameservers to use for domain name resolution. by default, attempts
	 * to read the system configuration. if that cannot be found, falls back to
	 * OpenDNS on 208.67.222.222 and 208.67.220.220
	 * all addresses must be valid ipv4 or ipv6 addresses
	 */
	@DefaultProvider(NameServersDefaultProvider.class)
	List<String> nameservers();
	
	@Default("JibbrJabbr") // hmmm want interpolations here.  maybe
	String userAgent();
}
