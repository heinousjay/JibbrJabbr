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
package jj.execution;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.logging.ExecutionTraceLogger;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.JJWebSocketConnection;

import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@Singleton
class ExecutionTraceImpl implements ExecutionTrace {
	
	private final Logger log;
	
	@Inject
	ExecutionTraceImpl(final @ExecutionTraceLogger Logger log) {
		this.log = log;
	}
	
	@Override
	public void preparingTask(OldJJTask<?> task) {
		
	}
	
	@Override
	public void startingTask(OldJJTask<?> task) {

	}
	
	@Override
	public void taskCompletedSuccessfully(OldJJTask<?> task) {

	}
	
	@Override
	public void taskCompletedWithError(OldJJTask<?> task, Throwable error) {

	}
	
	@Override
	public void start(HttpRequest request, HttpResponse response) {
		log.trace("Request started {}", request.uri());
	}
	
	@Override
	public void end(HttpRequest request, HttpResponse response) {
		log.trace("Request ended {}", request);
	}

	@Override
	public void start(JJWebSocketConnection connection) {
		log.trace("websocket connection started {}", connection);
	}
	
	@Override
	public void end(JJWebSocketConnection connection) {
		log.trace("websocket connection ended {}", connection);
	}
	
	@Override
	public void message(JJWebSocketConnection connection, String message) {
		log.trace("message from websocket connection {}", connection);
		log.trace(message);
	}
	
	@Override
	public void send(JJWebSocketConnection connection, String message) {
		log.trace("sending on websocket connection {}", connection);
		log.trace(message);
	}
	
	@Override
	public void startLessProcessing(String baseName) {
		log.trace("beginning processing of less servable at {}", baseName);
	}
	
	@Override
	public void loadLessResource(String resourceName) {
		log.trace("loading less resource {}", resourceName);
	}
	
	@Override
	public void errorLoadingLessResource(String resourceName, IOException io) {
		log.error("trouble loading less resource {}", resourceName);
		log.error("", io);
	}
	
	@Override
	public void finishLessProcessing(String baseName) {
		log.trace("finished processing less servable at {}", baseName);
	}
}
