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

import java.util.concurrent.Executor;

import javax.inject.Inject;

import jj.JJExecutors;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.stub.StubHttpControl;

/**
 * @author jason
 *
 */
class TestHttpControl extends StubHttpControl {

	private final JJExecutors executor;
	
	@Inject
	TestHttpControl(final JJExecutors executor, final TestHttpRequest request, final TestHttpResponse response) {
		super(request, response);
		this.executor = executor;
	}
	
	@Override
	public WebSocketConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
		return new TestHandlerConnection(handler);
	}
	
	@Override
	public Executor handlerExecutor() {
		return executor.httpControlExecutor();
	}
	
	public void nextHandler(HttpRequest request, HttpResponse response, HttpControl control) {
		throw new AssertionError(request + " not found");
	}
	
	@Override
	public TestHttpRequest request() {
		return (TestHttpRequest)super.request();
	}
	
	@Override
	public TestHttpResponse response() {
		return (TestHttpResponse)super.response();
	}
}
