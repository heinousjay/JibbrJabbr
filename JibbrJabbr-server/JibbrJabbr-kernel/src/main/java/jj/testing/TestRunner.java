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

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jsoup.nodes.Document;

import jj.webbit.JJEngineHttpHandler;

/**
 * @author jason
 *
 */
class TestRunner {
	
	private final class TestClientImpl implements TestClient {

		@Override
		public Document document() throws Exception {
			return control.response().get();
		}

		@Override
		public Document document(long timeout, TimeUnit unit) throws Exception {
			return control.response().get(timeout, unit);
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
					handler.handleHttpRequest(control.request(), control.response(), control);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return new TestClientImpl();
	}
	
	
}
