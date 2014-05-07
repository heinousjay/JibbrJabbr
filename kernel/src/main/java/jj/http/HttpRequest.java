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
package jj.http;

import io.netty.handler.codec.http.HttpMethod;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import jj.uri.URIMatch;

/**
 * @author jason
 *
 */
public interface HttpRequest {

	BigDecimal wallTime();

	String host();

	boolean secure();

	URI absoluteUri();

	String toString();

	/**
	 * @return
	 */
	long timestamp();

	/**
	 * @return
	 */
	String uri();
	
	URIMatch uriMatch();

	/**
	 * @param ifNoneMatch
	 * @return
	 */
	boolean hasHeader(String ifNoneMatch);

	/**
	 * @param etag
	 * @return
	 */
	String header(String name);

	/**
	 * @return
	 */
	String id();

	/**
	 * @return
	 */
	String body();

	/**
	 * @return
	 */
	Charset charset();

	/**
	 * @return
	 */
	HttpMethod method();

	/**
	 * @return
	 */
	List<Entry<String, String>> allHeaders();

	/**
	 * @param userAgent
	 * @param userAgent2
	 */
	HttpRequest header(String name, String value);
	
	Locale locale();

}