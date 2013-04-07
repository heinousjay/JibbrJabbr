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

/**
 * @author jason
 *
 */
public class ExecutionTrace {
	
	final static class State {
		
		private State() {}
	}
	
	private static final ThreadLocal<State> state = new ThreadLocal<ExecutionTrace.State>() {};
	
	static final void initiateState() {
		assert state.get() == null;
		state.set(new State());
	}

	public static void addEvent(final String event) {
		
	}
	
	public static void addEvent(final String event, final Throwable throwable) {
		
	}
}
