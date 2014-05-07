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

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import jj.uri.RouteFinder;
import jj.uri.URIMatch;
import jj.util.Sequence;

/**
 * @author jason
 *
 */
public abstract class AbstractHttpRequest implements HttpRequest {

	private static final Sequence sequence = new Sequence();
	
	private static final String HEADER_X_HOST = "x-host";
	
	private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
	
	protected final long startTime = System.nanoTime();
	
	protected final String id = sequence.next();
	
	protected final FullHttpRequest request;
	
	protected final String uri;
	
	protected final URIMatch uriMatch;

	/**
	 * 
	 */
	protected AbstractHttpRequest(final FullHttpRequest request, final RouteFinder routeFinder) {
		this.request = request;
		QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
		this.uri = routeFinder.find(QueryStringDecoder.decodeComponent(decoder.path()));
		this.uriMatch = new URIMatch(uri);
		
		// save off the query params into the form,
		// then if it's a verb that carries a body, read the body
		
	}

	@Override
	public BigDecimal wallTime() {
		return BigDecimal.valueOf(System.nanoTime() - startTime, 6);
	}
	
	/**
	 * @return
	 */
	@Override
	public String id() {
		return id;
	}

	@Override
	public String host() {
		String xHost = header(HEADER_X_HOST);
		String host = header(HttpHeaders.Names.HOST);
		return xHost == null ? host : xHost;
	}

	@Override
	public boolean secure() {
		return "https".equals(header(HEADER_X_FORWARDED_PROTO));
	}

	@Override
	public URI absoluteUri() {
		return URI.create(
			new StringBuilder("http")
				.append(secure() ? "s" : "")
				.append("://")
				.append(host())
				.append(uri())
				.toString()
		);
	}

	/**
	 * @return
	 */
	@Override
	public long timestamp() {
		return startTime;
	}

	/**
	 * @return
	 */
	@Override
	public Charset charset() {
		return StandardCharsets.UTF_8;
	}

	/**
	 * @return
	 */
	@Override
	public String uri() {
		return uri;
	}
	
	public URIMatch uriMatch() {
		return uriMatch;
	}

	/**
	 * @param ifNoneMatch
	 * @return
	 */
	@Override
	public boolean hasHeader(String ifNoneMatch) {
		return request.headers().contains(ifNoneMatch);
	}

	/**
	 * @param etag
	 * @return
	 */
	@Override
	public String header(String name) {
		return request.headers().get(name);
	}

	/**
	 * @param userAgent
	 * @param userAgent2
	 */
	@Override
	public HttpRequest header(String name, String value) {
		request.headers().add(name, value);
		return this;
	}

	/**
	 * @return
	 */
	@Override
	public String body() {
		return request.content().toString(charset());
	}

	/**
	 * @return
	 */
	@Override
	public HttpMethod method() {
		return request.getMethod();
	}

	/**
	 * @return
	 */
	@Override
	public List<Entry<String, String>> allHeaders() {
		return request.headers().entries();
	}
	
	@Override
	public Locale locale() {
		// TODO make this not hard-coded! haha
		return Locale.US;
	}

}