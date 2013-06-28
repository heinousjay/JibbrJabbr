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

import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
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
	void start(JJHttpRequest request, JJHttpResponse response);

	void end(JJHttpRequest request);

	void start(JJWebSocketConnection connection);
	
	void end(JJWebSocketConnection connection);

	void message(JJWebSocketConnection connection, String message);
	
	void send(JJWebSocketConnection connection, String message);
}