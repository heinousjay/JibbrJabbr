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

import jj.TaskCreator.JJTask;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.JJWebSocketConnection;

/**
 * @author jason
 *
 */
public class DevNullExecutionTraceImpl implements ExecutionTrace {

	@Override
	public void preparingTask(JJTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startingTask(JJTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskCompletedSuccessfully(JJTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskCompletedWithError(JJTask task, Throwable error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(HttpRequest request, HttpResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void end(HttpRequest request, HttpResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(JJWebSocketConnection connection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void end(JJWebSocketConnection connection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void message(JJWebSocketConnection connection, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(JJWebSocketConnection connection, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startLessProcessing(String baseName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadLessResource(String resourceName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishLessProcessing(String baseName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void errorLoadingLessResource(String resourceName, IOException io) {
		// TODO Auto-generated method stub
		
	}

}
