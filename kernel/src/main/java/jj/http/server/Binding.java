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
package jj.http.server;

import jj.util.StringUtils;

/**
 * @author jason
 *
 */
public class Binding {
	
	private final String host;
	private final int port;

	public Binding(int port) {
		this(null, port);
	}
	
	public Binding(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public int port() {
		return port;
	}
	
	public String host() {
		return host;
	}
	
	@Override
	public String toString() {
		return "bind(" +
			(host == null ? "" : "'" + host + "', ") +
			port +
			");";
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Binding && equals((Binding)obj);
	}
	
	public boolean equals(Binding other) {
		return StringUtils.equals(host, other.host) && port == other.port;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
