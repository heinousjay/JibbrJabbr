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
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

/**
 * @author jason
 *
 */
public class MockHttpRequest implements HttpRequest {

	@Override
	public HttpRequest data(String name, Object value) {
		return this;
	}

	@Override
	public Object data(String name) {
		return null;
	}

	@Override
	public boolean containsData(String name) {
		return false;
	}

	@Override
	public Object removeData(String name) {
		return null;
	}

	@Override
	public BigDecimal wallTime() {
		return null;
	}

	@Override
	public AssociatedScriptBundle associatedScriptBundle() {
		return null;
	}

	@Override
	public HttpRequest associatedScriptBundle(AssociatedScriptBundle associatedScriptBundle) {
		return this;
	}

	@Override
	public HttpRequest startingInitialExecution() {
		return this;
	}

	@Override
	public HttpRequest startingReadyFunction() {
		return this;
	}

	@Override
	public HttpRequestState state() {
		return null;
	}

	@Override
	public String host() {
		return null;
	}

	@Override
	public boolean secure() {
		return false;
	}

	@Override
	public URI absoluteUri() {
		return null;
	}

	@Override
	public HttpRequest addStartupJQueryMessage(JQueryMessage message) {
		return this;
	}

	@Override
	public List<JQueryMessage> startupJQueryMessages() {
		return null;
	}

	@Override
	public long timestamp() {
		return 0;
	}

	@Override
	public SocketAddress remoteAddress() {
		return null;
	}

	@Override
	public String uri() {
		return null;
	}

	@Override
	public boolean hasHeader(String ifNoneMatch) {
		return false;
	}

	@Override
	public String header(String name) {
		return null;
	}

	@Override
	public String id() {
		return null;
	}

	@Override
	public String body() {
		return null;
	}

	@Override
	public Charset charset() {
		return null;
	}

	@Override
	public HttpMethod method() {
		return null;
	}

	@Override
	public List<Entry<String, String>> allHeaders() {
		return null;
	}

	@Override
	public HttpRequest header(String name, String value) {
		return this;
	}

	@Override
	public HttpRequest method(HttpMethod method) {
		return this;
	}

	@Override
	public HttpRequest uri(String uri) {
		return this;
	}

}
