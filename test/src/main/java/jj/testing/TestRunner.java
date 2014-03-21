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
package jj.testing;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jj.http.server.TestHttpRequest;
import jj.http.server.TestHttpResponse;
import jj.http.server.TestJJEngineHttpHandler;
import jj.util.StringUtils;

/**
 * @author jason
 *
 */
class TestRunner {
	
	private final class TestHttpClientImpl implements TestHttpClient {
		
		
		private TestHttpClientImpl() {
		}
		
		private void getResponse() throws Exception {
			if (response.get()) {
				
			}
		}
		
		private void getResponse(final long timeout, final TimeUnit unit) throws Exception {
			if (response.get(timeout, unit)) {
				
			}
		}
		
		@Override
		public HttpResponseStatus status() throws Exception {
			testLog.trace("status() on {}", response.id());
			getResponse();
			return response.status();
		};
		
		@Override
		public HttpResponseStatus status(final long timeout, final TimeUnit unit) throws Exception {
			testLog.trace("status({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.status();
		};
		
		@Override
		public Throwable error() throws Exception {
			testLog.trace("error() on {}", response.id());
			getResponse();
			return response.error();
		}
		
		@Override
		public Throwable error(final long timeout, final TimeUnit unit) throws Exception {
			testLog.trace("error({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.error();
		}
		
		@Override
		public String contentsString() throws Exception {
			testLog.trace("contentsString() on {}", response.id());
			getResponse();
			return response.contentsString();
		}

		@Override
		public String contentsString(final long timeout, final TimeUnit unit) throws Exception {
			testLog.trace("contentsString({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.contentsString();
		}

		@Override
		public Document document() throws Exception {
			testLog.trace("document() on {}", response.id());
			getResponse();
			return response.document();
		}

		@Override
		public Document document(final long timeout, final TimeUnit unit) throws Exception {
			testLog.trace("document({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.document();
		}
		
		@Override
		public TestHttpClient[] requestAllDependencies() throws Exception {
			List<TestHttpClient> result = new ArrayList<>();
			Document document = document();
			if (document != null) {
				Path basePath = Paths.get(uri());
				for (Element element : document.select("link[href],script[src],img[src]")) {
					String href = element.attr("href");
					if (StringUtils.isEmpty(href)) href = element.attr("src");
					result.add(app.get(basePath.resolveSibling(href).toAbsolutePath().toString()));
				}
			}
			return result.toArray(new TestHttpClient[result.size()]);
		}
		
		@Override
		public void dumpObjects() {
			testLog.trace("{}", request);
			testLog.trace("{}", response);
		}
		
		@Override
		public String uri() {
			return request.uri();
		}

		@Override
		public boolean matchesHeaders(HttpHeaders headers) throws Exception {
			testLog.trace("matchesHeaders() on {} with \n{}", response.id(), response.headers());
			response.get();
			return true;
		}

		@Override
		public byte[] contentBytes() throws Exception {
			testLog.trace("contentBytes() on {} with \n{}", response.id(), response.headers());
			response.get();
			return response.contentBytes();
		}

		@Override
		public byte[] contentBytes(long timeout, TimeUnit unit) throws Exception {
			testLog.trace("contentBytes() on {} with \n{}", response.id(), response.headers());
			response.get(timeout, unit);
			return response.contentBytes();
		}
	}
	
	private final JibbrJabbrTestServer app;
	private final TestHttpRequest request;
	private final TestHttpResponse response;
	private final TestJJEngineHttpHandler handler;
	private final TestLog testLog;
	
	@Inject
	TestRunner(
		final JibbrJabbrTestServer app,
		final TestHttpRequest request,
		final TestHttpResponse response,
		final TestJJEngineHttpHandler handler,
		final TestLog testLog
	) {
		this.app = app;
		this.request = request;
		this.response = response;
		this.handler = handler;
		this.testLog = testLog;
	}

	TestHttpRequest request() {
		return request;
	}
	
	TestHttpClient run() {
		try {
			testLog.info("starting {}", request);
			handler.handleHttpRequest(request, response);
		} catch (Throwable t) {
			response.error(t);
		}
		return new TestHttpClientImpl();
	}
	
	
}
