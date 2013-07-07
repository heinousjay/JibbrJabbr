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
package jj;

import java.io.IOException;

import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.JJWebSocketConnection;

/**
 * @author jason
 *
 */
public interface ExecutionTrace {
	
	void preparingTask(JJRunnable oldTask, JJRunnable newTask);
	
	void startingTask(JJRunnable task);
	
	void taskCompletedSuccessfully(JJRunnable task);
	
	void taskCompletedWithError(JJRunnable task, Throwable error);
	
	/**
	 * @param request
	 * @param response
	 */
	void start(HttpRequest request, HttpResponse response);

	void end(HttpRequest request, HttpResponse response);

	void start(JJWebSocketConnection connection);
	
	void end(JJWebSocketConnection connection);

	void message(JJWebSocketConnection connection, String message);
	
	void send(JJWebSocketConnection connection, String message);
	
	
	void startLessProcessing(String baseName);
	
	void loadLessResource(String resourceName);
	
	void finishLessProcessing(String baseName);

	/**
	 * @param resourceName
	 * @param io
	 */
	void errorLoadingLessResource(String resourceName, IOException io);
}