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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class RhinoContext implements Closeable {

	private final Context context;
	private boolean closed = false;
	
	RhinoContext(final Context context) {
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
	
	public Scriptable initStandardObjects() {
		assertNotClosed();
		return context.initStandardObjects();
	}
	
	public Function compileFunction(final Scriptable scope, final String source, final String sourceName) {
		assertNotClosed();
		return context.compileFunction(scope, source, sourceName, 1, null);
	}

	/**
	 * @param configurationFunction
	 * @param scope
	 * @param scope2
	 */
	public Object callFunction(Function configurationFunction, Scriptable scope, Scriptable thisObj, Object...args) {
		assertNotClosed();
		return configurationFunction.call(context, scope, thisObj, args);
	}
}