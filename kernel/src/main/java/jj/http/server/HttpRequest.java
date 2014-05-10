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

import io.netty.handler.codec.http.HttpMethod;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import jj.uri.URIMatch;

/**
 * Represents the incoming HTTP request
 * 
 * @author jason
 *
 */
public interface HttpRequest {

	/**
	 * The number of milliseconds since the request was received by the engine,
	 * according to the wall.  This has nothing to do with how much processing
	 * the request has received, just the latency.
	 * @return
	 */
	BigDecimal wallTime();

	/**
	 * The host from the request, either from the X-Host header, or the
	 * Host header if that is not present.
	 * @return
	 */
	String host();

	/**
	 * Flag if the request is secure.  Since JibbrJabbr does not support SSL
	 * natively, this relies on the presence of the X-Forwarded-Proto header
	 * in the request from the proxy.  Since this is potentially spoofable,
	 * don't use it for anything too meaningful. TODO see if spoofability is
	 * for real-real
	 * @return
	 */
	boolean secure();

	/**
	 * The absolute URI of the request
	 * @return
	 */
	URI absoluteUri();

	/**
	 * {@link System#nanoTime()} when the request was received
	 */
	long timestamp();

	/**
	 * The URI from the initial line
	 */
	String uri();
	
	/**
	 * A {@link URIMatch} of the request's URI
	 */
	URIMatch uriMatch();

	/**
	 * check for the presence of a header.  case-insensitive
	 */
	boolean hasHeader(String headerName);

	/**
	 * The first value of the given header
	 */
	String header(String name);

	/**
	 * A unique ID for the request
	 */
	String id();

	/**
	 * Ehhhh this sucks.
	 */
	String body();

	/**
	 * The character set of the request, currently just assumed to be UTF-8.
	 */
	Charset charset();

	/**
	 * The method from the initial line
	 */
	HttpMethod method();

	/**
	 * Every header from the request
	 */
	List<Entry<String, String>> allHeaders();
	
	/**
	 * The locale requested.  This gets a little complicated
	 * @return
	 */
	Locale locale();
	
	public interface Cookie {
		String name();
		String value();
	}
	
	Cookie cookie(String name);
	
	List<Cookie> cookies();
}