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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.ExecutionTrace;
import jj.webbit.JJEngineHttpHandler;

/**
 * @author jason
 *
 */
class TestRunner {
	
	static final Logger log = LoggerFactory.getLogger(TestRunner.class);
	
	private final class TestClientImpl implements TestClient {
		
		private final TestHttpResponse response;
		
		private TestClientImpl(final TestHttpResponse response) {
			this.response = response;
		}
		
		@Override
		public int status() throws Exception {
			log.trace("status() on {}", response.id());
			response.get();
			return response.status();
		};
		
		@Override
		public int status(final long timeout, final TimeUnit unit) throws Exception {
			log.trace("status({}, {}) on {}", timeout, unit, response.id());
			response.get(timeout, unit);
			return response.status();
		};
		
		@Override
		public Throwable error() throws Exception {
			log.trace("error() on {}", response.id());
			response.get();
			return response.error();
		}
		
		@Override
		public Throwable error(final long timeout, final TimeUnit unit) throws Exception {
			log.trace("error({}, {}) on {}", timeout, unit, response.id());
			response.get(timeout, unit);
			return response.error();
		}
		
		@Override
		public Map<String, String> headers() throws Exception {
			log.trace("headers() on {}", response.id());
			response.get();
			return response.headers();
		}
		
		@Override
		public Map<String, String> headers(final long timeout, final TimeUnit unit) throws Exception {
			log.trace("headers({}, {}) on {}", timeout, unit, response.id());
			response.get(timeout, unit);
			return response.headers();
		}
		
		@Override
		public String contentsString() throws Exception {
			log.trace("contentsString() on {}", response.id());
			response.get();
			return response.contentsString();
		}

		@Override
		public String contentsString(final long timeout, final TimeUnit unit) throws Exception {
			log.trace("contentsString({}, {}) on {}", timeout, unit, response.id());
			response.get(timeout, unit);
			return response.contentsString();
		}

		@Override
		public Document document() throws Exception {
			log.trace("document() on {}", response.id());
			response.get();
			return response.document();
		}

		@Override
		public Document document(final long timeout, final TimeUnit unit) throws Exception {
			log.trace("document({}, {}) on {}", timeout, unit, response.id());
			response.get(timeout, unit);
			return response.document();
		}
		
		@Override
		public void dumpObjects() {
			log.trace("{}", control);
			log.trace("{}", control.request());
			log.trace("{}", response);
		}
	}
	
	private final TestHttpControl control;
	private final JJEngineHttpHandler handler;
	
	@Inject
	TestRunner(
		final TestHttpControl control,
		final JJEngineHttpHandler handler
	) {
		this.control = control;
		this.handler = handler;
	}

	TestHttpRequest request() {
		return control.request();
	}
	
	TestClient run() {
		control.execute(new Runnable() {
			public void run() {
				try {
					ExecutionTrace.start(control.request(), control.response());
					handler.handleHttpRequest(control.request(), control.response(), control);
				} catch (Throwable t) {
					control.response().error(t);
				}
			}
		});
		return new TestClientImpl(control.response());
	}
	
	
}
