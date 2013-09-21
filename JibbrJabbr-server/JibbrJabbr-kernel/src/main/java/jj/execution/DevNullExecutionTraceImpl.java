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

import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.JJWebSocketConnection;

/**
 * @author jason
 *
 */
public class DevNullExecutionTraceImpl implements ExecutionTrace {

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
		
	}

	@Override
	public void end(HttpRequest request, HttpResponse response) {
		
	}

	@Override
	public void start(JJWebSocketConnection connection) {
		
	}

	@Override
	public void end(JJWebSocketConnection connection) {
		
	}

	@Override
	public void message(JJWebSocketConnection connection, String message) {
		
	}

	@Override
	public void send(JJWebSocketConnection connection, String message) {
		
	}

	@Override
	public void startLessProcessing(String baseName) {
		
	}

	@Override
	public void loadLessResource(String resourceName) {
		
	}

	@Override
	public void finishLessProcessing(String baseName) {
		
	}

	@Override
	public void errorLoadingLessResource(String resourceName, IOException io) {
		
	}

}
