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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

/**
 * attempts to find the configured system nameservers, falling back to
 * opendns if it can't
 * 
 * @author jason
 *
 */
public class NameServersDefaultProvider implements Provider<List<String>> {
	
	private static final List<String> OPEN_DNS = Arrays.asList("208.67.222.222", "208.67.220.220");
	
	private final List<String> nameservers;
	
	@SuppressWarnings("unchecked")
	NameServersDefaultProvider() {
		
		// fall back on OpenDNS if there is no system configuration
		List<String> result = OPEN_DNS;
		
		try {
			Class<?> c = Class.forName("sun.net.dns.ResolverConfiguration");
			
			Object instance = c.getMethod("open").invoke(null);
			result = (List<String>)c.getMethod("nameservers").invoke(instance);
		} catch (Exception e) {}
		
		if (result.isEmpty()) {
			result = OPEN_DNS;
		}
		
		nameservers = Collections.unmodifiableList(result);
	}

	@Override
	public List<String> get() {
		
		return nameservers;
	}

}
