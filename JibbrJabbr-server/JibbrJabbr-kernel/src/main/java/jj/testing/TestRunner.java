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
import jj.ExecutionTrace;
import jj.logging.TestRunnerLogger;
import jj.webbit.JJEngineHttpHandler;

/**
 * @author jason
 *
 */
class TestRunner {
	
	private final Logger testRunnerLog;
	
	private final class TestHttpClientImpl implements TestHttpClient {
		
		private final TestHttpResponse response;
		
		private TestHttpClientImpl(final TestHttpResponse response) {
			this.response = response;
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
		public int status() throws Exception {
			testRunnerLog.trace("status() on {}", response.id());
			getResponse();
			return response.status();
		};
		
		@Override
		public int status(final long timeout, final TimeUnit unit) throws Exception {
			testRunnerLog.trace("status({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.status();
		};
		
		@Override
		public Throwable error() throws Exception {
			testRunnerLog.trace("error() on {}", response.id());
			getResponse();
			return response.error();
		}
		
		@Override
		public Throwable error(final long timeout, final TimeUnit unit) throws Exception {
			testRunnerLog.trace("error({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.error();
		}
		
		@Override
		public Map<String, String> headers() throws Exception {
			testRunnerLog.trace("headers() on {}", response.id());
			getResponse();
			return response.headers();
		}
		
		@Override
		public Map<String, String> headers(final long timeout, final TimeUnit unit) throws Exception {
			testRunnerLog.trace("headers({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.headers();
		}
		
		@Override
		public String contentsString() throws Exception {
			testRunnerLog.trace("contentsString() on {}", response.id());
			getResponse();
			return response.contentsString();
		}

		@Override
		public String contentsString(final long timeout, final TimeUnit unit) throws Exception {
			testRunnerLog.trace("contentsString({}, {}) on {}", timeout, unit, response.id());
			getResponse(timeout, unit);
			return response.contentsString();
		}

		@Override
		public Document document() throws Exception {
			testRunnerLog.trace("document() on {}", response.id());
			response.get();
			return response.document();
		}

		@Override
		public Document document(final long timeout, final TimeUnit unit) throws Exception {
			testRunnerLog.trace("document({}, {}) on {}", timeout, unit, response.id());
			response.get(timeout, unit);
			return response.document();
		}
		
		@Override
		public void dumpObjects() {
			testRunnerLog.trace("{}", control);
			testRunnerLog.trace("{}", control.request());
			testRunnerLog.trace("{}", response);
		}
	}
	
	private final TestHttpControl control;
	private final JJEngineHttpHandler handler;
	private final ExecutionTrace trace;
	
	@Inject
	TestRunner(
		final TestHttpControl control,
		final JJEngineHttpHandler handler,
		final ExecutionTrace trace,
		final @TestRunnerLogger Logger testRunnerLog
	) {
		this.control = control;
		this.handler = handler;
		this.trace = trace;
		this.testRunnerLog = testRunnerLog;
	}

	TestHttpRequest request() {
		return control.request();
	}
	
	TestHttpClient run() {
		control.execute(new Runnable() {
			public void run() {
				try {
					trace.start(control.request(), control.response());
					handler.handleHttpRequest(control.request(), control.response(), control);
				} catch (Throwable t) {
					control.response().error(t);
				}
			}
		});
		return new TestHttpClientImpl(control.response());
	}
	
	
}
