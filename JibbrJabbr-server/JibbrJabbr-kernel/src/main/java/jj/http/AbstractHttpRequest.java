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

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import jj.DateFormatHelper;
import jj.Sequence;
import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

/**
 * @author jason
 *
 */
abstract class AbstractHttpRequest implements HttpRequest {

	private static final Sequence sequence = new Sequence();
	
	private static final String HEADER_X_HOST = "x-host";
	
	private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
	
	protected final HashMap<String, Object> data = new HashMap<>();
	
	protected final long startTime = System.nanoTime();
	
	protected HttpRequestState state = HttpRequestState.Uninitialized;
	
	protected AssociatedScriptBundle associatedScriptBundle;
	
	protected final String id = sequence.next();
	
	protected ArrayList<JQueryMessage> messages;
	
	protected final FullHttpRequest request;

	/**
	 * 
	 */
	AbstractHttpRequest(final FullHttpRequest request) {
		this.request = request;
	}

	@Override
	public HttpRequest data(String name, Object value) {
		data.put(name, value);
		return this;
	}

	@Override
	public Object data(String name) {
		return data.get(name);
	}

	@Override
	public boolean containsData(String name) {
		return data.containsKey(name);
	}

	@Override
	public Object removeData(String name) {
		return data.remove(name);
	}

	@Override
	public BigDecimal wallTime() {
		return BigDecimal.valueOf(System.nanoTime() - startTime, 6);
	}

	@Override
	public AssociatedScriptBundle associatedScriptBundle() {
		return associatedScriptBundle;
	}

	@Override
	public HttpRequest associatedScriptBundle(AssociatedScriptBundle associatedScriptBundle) {
		this.associatedScriptBundle = associatedScriptBundle;
		return this;
	}

	@Override
	public HttpRequest startingInitialExecution() {
		state = HttpRequestState.InitialExecution;
		associatedScriptBundle().initializing(true);
		return this;
	}

	@Override
	public HttpRequest startingReadyFunction() {
		state = HttpRequestState.ReadyFunctionExecution;
		return this;
	}

	@Override
	public HttpRequestState state() {
		return state;
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
	 * adds a message intended to be processed a framework startup
	 * on the client.  initially intended for event bindings but
	 * some other case may come up
	 * @param message
	 */
	@Override
	public HttpRequest addStartupJQueryMessage(final JQueryMessage message) {
		if (messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(message);
		return this;
	}

	@Override
	public List<JQueryMessage> startupJQueryMessages() {
		ArrayList<JQueryMessage> messages = this.messages;
		this.messages = null;
		return messages == null ? Collections.<JQueryMessage>emptyList() : messages;
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

	@Override
	public String toString() {
		return "httpRequest[" +
			remoteAddress() +
			"] started at " +
			DateFormatHelper.basicFormat(timestamp()) +
			" with data " +
			data;
	}

	/**
	 * @return
	 */
	@Override
	public String uri() {
		return request.getUri();
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

	/**
	 * @param userAgent
	 * @param userAgent2
	 */
	@Override
	public HttpRequest header(String name, String value) {
		request.headers().add(name, value);
		return this;
	}

}