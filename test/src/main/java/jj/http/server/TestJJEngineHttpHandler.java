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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.google.inject.Injector;

import jj.execution.TaskRunner;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.EngineHttpHandler;
import jj.http.server.servable.Servables;
import jj.logging.SystemLogger;

/**
 * just exposing a method for testing
 * @author jason
 *
 */
@Singleton
public class TestJJEngineHttpHandler extends EngineHttpHandler {

	/**
	 * @param taskRunner
	 * @param resourceTypes
	 */
	@Inject
	TestJJEngineHttpHandler(
		final TaskRunner taskRunner,
		final Servables servables,
		final Injector injector,
		final WebSocketRequestChecker webSocketUriChecker,
		final SystemLogger logger
	) {
		super(taskRunner, servables, injector, webSocketUriChecker, logger);
	}

	@Override
	public void handleHttpRequest(HttpRequest request, HttpResponse response) throws Exception {
		super.handleHttpRequest(request, response);
	}
}
