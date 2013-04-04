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
package jj.webbit;

import jj.JJExecutors;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.stub.StubHttpControl;

/**
 * Bridges between Webbit and the test interface
 * 
 * @author jason
 *
 */
public class WebbitTestRunner {

	private final JJExecutors jjExecutors;
	
	private final JJEngineHttpHandler htmlEngineHttpHandler;
	
	private Exception testException = null;
	
	WebbitTestRunner(
		final JJExecutors jjExecutors,
		final JJEngineHttpHandler htmlEngineHttpHandler
	) {
		this.jjExecutors = jjExecutors;
		this.htmlEngineHttpHandler = htmlEngineHttpHandler;
	}
	
	public void executeRequest(final HttpRequest request, final HttpResponse response) throws Exception {
		
		jjExecutors.httpControlExecutor().submit(new Runnable() {

			@Override
			public void run() {
				try {
					htmlEngineHttpHandler.handleHttpRequest(request, response, new StubHttpControl() {
						
						public void nextHandler(HttpRequest request, HttpResponse response, HttpControl control) {
							
							throw new AssertionError(request);
						}
						
						@Override
						public void execute(Runnable command) {
							jjExecutors.httpControlExecutor().execute(command);
						}
					});
				} catch (Exception e) {
					testException = e;
				} 
			}
		});
		
		if (testException != null) {
			throw testException;
		}
	}
}
