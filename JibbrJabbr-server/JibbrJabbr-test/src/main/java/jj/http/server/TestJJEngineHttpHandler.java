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
package jj.http.server;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.google.inject.Injector;

import jj.execution.ExecutionTrace;
import jj.execution.JJExecutors;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.JJEngineHttpHandler;
import jj.http.server.WebSocketConnectionMaker;
import jj.http.server.servable.Servable;
import jj.logging.EmergencyLogger;

/**
 * just exposing a method for testing
 * @author jason
 *
 */
@Singleton
public class TestJJEngineHttpHandler extends JJEngineHttpHandler {

	/**
	 * @param executors
	 * @param resourceTypes
	 */
	@Inject
	TestJJEngineHttpHandler(
		final JJExecutors executors,
		final Set<Servable> resourceTypes,
		final Injector injector,
		final ExecutionTrace trace,
		final WebSocketUriChecker webSocketUriChecker,
		final WebSocketConnectionMaker webSocketConnectionMaker,
		final @EmergencyLogger Logger logger
	) {
		super(executors, resourceTypes, injector, trace, webSocketUriChecker, webSocketConnectionMaker, logger);
	}

	@Override
	public void handleHttpRequest(HttpRequest request, HttpResponse response) throws Exception {
		super.handleHttpRequest(request, response);
	}
}
