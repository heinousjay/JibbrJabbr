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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.util.concurrent.Future;

/**
 * @author jason
 *
 */
@Singleton
public class HttpRequester {
	
	private static final Map<String, Integer> SERVICES;
	
	static {
		Map<String, Integer> m = new HashMap<>();
		m.put("http", 80);
		m.put("https", 443);
		SERVICES = Collections.unmodifiableMap(m);
	}
	
	private final HttpClient client;
	
	@Inject
	HttpRequester(final HttpClient client) {
		this.client = client;
	}
	
	public Method requestTo(String uri) {
		try {
			return new Method(URI.create(uri));
		} catch (Exception e) {
			throw new AssertionError("ALWAYS VALIDATE THE URI BEFORE STARTING THE REQUEST");
		}
	}
	
	public class Method {
		
		private final URI uri;
		private final Map<String, List<String>> params = new LinkedHashMap<>();
		
		private Method(URI uri) {
			this.uri = uri;
		}
		
		public Method param(String name, String value) {
			params.computeIfAbsent(name, n -> { return new ArrayList<>(1); }).add(value);
			return this;
		}
		
		public Headers get() {
			return method(HttpMethod.GET);
		}
		
		public Headers post() {
			return method(HttpMethod.POST);
		}
		
		public Headers put() {
			return method(HttpMethod.PUT);
		}
		
		public Headers delete() {
			return method(HttpMethod.DELETE);
		}
		
		public Headers method(HttpMethod method) {

			// validate the URI!
			boolean secure = "https".equals(uri.getScheme());
			
			int port = uri.getPort();
			port = port == -1 ? (SERVICES.get(uri.getScheme())) : port;
			
			return new Headers(secure, uri.getHost(), port, method, makeUri(uri));
		}
		
		private String makeUri(URI u) {
			StringBuilder path = new StringBuilder(u.getPath());
			
			for (Iterator<String> paramIter = params.keySet().iterator(); paramIter.hasNext();) {

				String p = paramIter.next();
				processParam(path, paramIter, p);
			}
			
			try {
				URLEncoder.encode(path.toString(), UTF_8.name()).replace("+", "%20");
			} catch (UnsupportedEncodingException e) {
				throw new AssertionError(e);
			}
			
			QueryStringEncoder qse = new QueryStringEncoder(path.toString());
			
			params.entrySet().forEach( entry -> {
				entry.getValue().forEach(value -> {
					qse.addParam(entry.getKey(), value);
				});
			});
			
			return qse.toString();
		}
		
		private String popParam(String name) {
			try {
				return URLEncoder.encode(params.get(name).remove(0), UTF_8.name()).replace("+", "%20");
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}

		private boolean processParam(StringBuilder sb, Iterator<String> paramIter, String p) {
			int index = sb.indexOf(":" + p);
			if (index > -1) {
				sb.replace(index, index + p.length() + 1, popParam(p));
				if (params.get(p).isEmpty()) paramIter.remove();
				return true;
			}
			return false;
		}
	}
	
	public class Headers {
		
		private final boolean secure;
		private final String host;
		private final int port;
		private final DefaultHttpRequest request;
		
		private Headers(boolean secure, String host, int port, HttpMethod httpMethod, String uri) {
			this.secure = secure;
			this.host = host;
			this.port = port;
			request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri);
			request.headers().add(HttpHeaders.Names.HOST, host);
		}
	
		public Headers header(CharSequence name, CharSequence value) {
			request.headers().add(name, value);
			return this;
		}
		
		private Channel ch(Future<?> future) {
			return ((ChannelFuture) future).channel();
		}

		public void begin(HttpResponseListener listener) throws Exception {
			client.connect(secure, host, port).addListener(future -> {
				Channel ch = ch(future);
				ch.writeAndFlush(request).addListener(f -> {
					if (!f.isSuccess()) {
						listener.requestErrored(f.cause());
					} else {
						ch.pipeline().addLast(listener.handler());
					}
				});
			});
		}
	}
}
