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

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.logging.ExecutionTraceLogger;
import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
import jj.http.JJWebSocketConnection;

import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@Singleton
class ExecutionTraceImpl implements ExecutionTrace {
	
	private static final class State {
		private JJRunnable prepared;
		private JJRunnable current;
		private JJHttpRequest request;
		
		@Override
		public String toString() {
			return new StringBuilder()
				.append("prepared=").append(prepared)
				.append(",current=").append(current)
				.append(",request=").append(request)
				.toString();
		}
	}
	
	private final ThreadLocal<State> current = new ThreadLocal<State>();
	private final ConcurrentHashMap<JJRunnable, State> currentTracker = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<JJRunnable, State> preparedTracker = new ConcurrentHashMap<>();
	
	private final Logger log;
	
	@Inject
	ExecutionTraceImpl(final @ExecutionTraceLogger Logger log) {
		this.log = log;
	}
	
	@Override
	public void preparingTask(JJRunnable oldTask, JJRunnable newTask) {
		State state;
		if (oldTask != null) {
			log.trace("task [{}] is preparing [{}]", oldTask, newTask);
			state = currentTracker.get(oldTask);
			
			assert state != null : "preparing a JJRunnable from a JJRunnable but no state is in place";
			assert state.prepared == null : "preparing a JJRunnable but one is already prepared";
			assert state.current == oldTask : "state data is all screwed up";
			
		} else {
			log.trace("thread [{}] is preparing [{}]", Thread.currentThread().getName(), newTask);
			
			assert !currentTracker.containsKey(newTask) : "preparing a JJRunnable from outside but there is already a current task";
			
			state = new State();
		}
		state.prepared = newTask;
		boolean wasAbleToStore = (preparedTracker.putIfAbsent(newTask, state) == null);
		
		assert wasAbleToStore : "task [" + newTask + "] had previous prepared state";
	}
	
	@Override
	public void startingTask(JJRunnable task) {
		
		log.trace("starting task [{}]", task);
		State state = preparedTracker.get(task);
		
		assert state != null && state.prepared == task : "starting a task [" + task + "] that wasn't prepared";
		
		JJRunnable oldTask = state.current;
		if (oldTask != null) {
			boolean removed = currentTracker.remove(oldTask, state);
			
			assert removed : "old task [" + task + "] mapping in current tracker was not correct";
		}

		boolean removed = preparedTracker.remove(task, state);
		
		assert removed : "old task [" + task + "] mapping in prepared tracker was not correct";
		
		state.current = task;
		state.prepared = null;
		
		currentTracker.putIfAbsent(task, state);
		assert current.get() == null : "state is already stored in current";
		current.set(state);
	}
	
	@Override
	public void taskCompletedSuccessfully(JJRunnable task) {
		log.trace("successful completion of task [{}]", task);
		
		current.set(null);
		
		State state = currentTracker.get(task);
		assert state != null : "task ended and no state.  que?";
		if (state.prepared == null) {
			boolean removed = currentTracker.remove(task, state);
			assert removed : "something is really whacky here";
			log.trace("end processing");
		}
	}
	
	@Override
	public void taskCompletedWithError(JJRunnable task, Throwable error) {
		
		log.error("task [{}] completed with error:", task);
		log.error("", error);
		
		current.set(null);
		
		State state = currentTracker.get(task);
		assert state != null : "task ended and no state.  que?";
		if (state.prepared == null) {
			boolean removed = currentTracker.remove(task, state);
			assert removed : "something is really whacky here";
			log.trace("end processing");
		}
	}
	
	@Override
	public void start(JJHttpRequest request, JJHttpResponse response) {
		log.trace("Request started {}", request);
		State state = current.get();
		assert state == null : "somehow there is state left from some previous request " + state;
		
		
		//state = new State();
		//state.request = request;
		//current.set(state);
	}
	
	@Override
	public void end(JJHttpRequest request) {
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
}
