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
package jj.script;

import java.io.Closeable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;

/**
 * Closeable wrapper for a context
 * @author jason
 *
 */
@Singleton
public class RhinoContextMaker {
	
	public static class RhinoContext implements Closeable {

		private final Context context;
		private boolean closed = false;
		
		private RhinoContext(final Context context) {
			this.context = context;
		}
		
		private void assertNotClosed() {
			assert !closed : "no performing operations on a context that has been closed!";
		}
		
		@Override
		public void close() {
			closed = true;
			Context.exit();
		}
		
		public ContinuationPending captureContinuation() {
			assertNotClosed();
			return context.captureContinuation();
		}
	}
	
	@Inject
	RhinoContextMaker() {}

	public RhinoContext context() {
		return new RhinoContext(Context.enter());
	}
}
