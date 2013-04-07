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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebSocketConnection;

/**
 * @author jason
 *
 */
public class ExecutionTrace {
	
	private static final Logger log = LoggerFactory.getLogger("execution trace");
	
	private final static class Event {
		private final long at = System.currentTimeMillis();
		private final String description;
		private final Throwable throwable;
		
		private Event(final String description) {
			this(description, null);
		}
		
		private Event(final String description, final Throwable throwable) {
			this.description = description;
			this.throwable = throwable;
		}
		
		@Override
		public String toString() {
			return new StringBuilder(description.length() + 30)
				.append("event at ")
				.append(DateFormatHelper.basicFormat(at))
				.append(" - ")
				.append(description)
				.append(throwable == null ? "" : " with throwable ")
				.append(throwable == null ? "" : throwable.getMessage())
				.toString();
		}
	}
	
	private static final Sequence IDS = new Sequence();
	
	final static class State {
		
		private State() {}
		
		private final String id = IDS.next();
		private final long start = System.currentTimeMillis();
		private HttpRequest httpRequest;
		private HttpResponse httpResponse;
		private final List<Event> events = new ArrayList<>();
		
		private void outputEvents(final StringBuilder s) {
			s.append("Events:\n");
			for (Event event : events) {
				s.append("  - ").append(event).append("\n");
			}
		}
		
		@Override
		public String toString() {
			StringBuilder s = new StringBuilder(384)
				.append("Execution trace [").append(id).append("]")
				.append("http request started at ")
				.append(DateFormatHelper.basicFormat(start)).append("\n")
				.append(httpRequest).append("\n")
				.append(httpResponse).append("\n");
			outputEvents(s);
			return s.toString();
		}
	}
	
	private static final ThreadLocal<State> state = new ThreadLocal<ExecutionTrace.State>() {};
	
	private static void initializeState() {
		//assert state.get() == null : state.get();
		//state.set(new State());
	}
	
	static State save() {
		return null;
//		State s = state.get();
//		if (s != null) {
//			log.info("saving {} on {}", s.id, Thread.currentThread().getName());
//			state.set(null);
//		} else {
//			log.info("asked to save but nothing here on {}", Thread.currentThread().getName());
//			log.info("", new Exception());
//		}
//		return s;
	}
	
	static void restore(final State s) {
//		if (s != null) {
//			log.info("restoring {} on {}", s.id, Thread.currentThread().getName());
//			assert state.get() == null : state.get();
//			state.set(s);
//		} else {
//			log.info("asked to restore but nothing here on {}", Thread.currentThread().getName());
//			log.info("", new Exception());
//		}
	}

	public static void addEvent(final String event) {
//		State s = state.get();
//		assert s != null;
//		s.events.add(new Event(event));
	}
	
	public static void addEvent(final String event, final Throwable throwable) {
//		State s = state.get();
//		assert s != null;
//		s.events.add(new Event(event, throwable));
	}

	/**
	 * @param request
	 * @param response
	 */
	public static void start(final HttpRequest request, final HttpResponse response) {
//		initializeState();
//		State s = state.get();
//		log.info("starting {} on {}", s.id, Thread.currentThread().getName());
//		log.trace("request - {}", request);
//		s.httpRequest = request;
//		s.httpResponse = response;
	}
	
	public static void end(final HttpRequest request) {
//		State s = state.get();
//		assert s != null;
//		state.set(null);
//		log.info("ending {} on {}", s.id, Thread.currentThread().getName());
//		log.trace("request - {}", request);
//		assert s.httpRequest == request;
//		log.info("{}", s);
	}
	
	public static void start(WebSocketConnection connection) {
//		initializeState();
//		State s = state.get();
//		log.info("starting {} on {}", s.id, Thread.currentThread().getName());
//		log.trace("connection - {}", connection);
//		s.httpRequest = request;
//		s.httpResponse = response;
	}
}
