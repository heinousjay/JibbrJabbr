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

import javax.inject.Inject;

import jj.event.Publisher;
import jj.util.Closer;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.json.JsonParser;

/**
 * Thin wrapper around the rhino context mechanism to take advantage of
 * try-with-resources, and to facilitate mocking and ensure all script
 * errors are logged
 * @author jason
 *
 */
public class RhinoContext implements Closer {

	private final Context context;
	private boolean closed = false;
	
	private final Publisher publisher;
	
	@Inject
	RhinoContext(
		final Publisher publisher
	) {
		this.context = Context.enter();
		this.publisher = publisher;
		
		context.setLanguageVersion(Context.VERSION_1_8);
		context.setOptimizationLevel(-1);
	}

	public RhinoContext withoutContinuations() {
		assertNotClosed();
		context.setOptimizationLevel(1);
		return this;
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
	
	public ScriptableObject initStandardObjects() {
		assertNotClosed();
		return context.initStandardObjects();
	}

	public ScriptableObject initStandardObjects(boolean sealed) {
		assertNotClosed();
		return context.initStandardObjects(null, sealed);
	}
	
	public ScriptableObject newObject(Scriptable scope) {
		assertNotClosed();
		return (ScriptableObject)context.newObject(scope);
	}
	
	public JsonParser newJsonParser(Scriptable scope) {
		assertNotClosed();
		return new JsonParser(context, scope);
	}
	
	private void publishRhinoException(String description, RhinoException re) {
		publisher.publish(new ScriptError(description, re));
	}
	
	public Function compileFunction(final Scriptable scope, final String source, final String sourceName) {
		assertNotClosed();
		try {
			return context.compileFunction(scope, source, sourceName, 1, null);
		} catch (RhinoException re) {
			publishRhinoException("script error compiling a function\n{}", re);
			throw re;
		}
	}

	public Script compileString(final String source, final String sourceName) {
		assertNotClosed();
		try {
			return context.compileString(source, sourceName, 1, null);
		} catch (RhinoException re) {
			publishRhinoException("script error compiling a function\n{}", re);
			throw re;
		}
	}

	public Object callFunction(Function function, Scriptable scope, Scriptable thisObj, Object...args) {
		assertNotClosed();
		try {
			return function.call(context, scope, thisObj, args);
		} catch (RhinoException re) {
			publishRhinoException("script error executing a function\n{}\n{}", re);
			throw re;
		}
	}

	public Object evaluateString(Scriptable scope, String source, String sourceName) {
		assertNotClosed();
		try {
			return context.evaluateString(scope, source, sourceName, 1, null);
		} catch (RhinoException re) {
			publishRhinoException("script error evaluating a string\n{}\n{}", re);
			throw re;
		}
	}
	
	public Object executeScript(Script script, Scriptable scope) {
		assertNotClosed();
		try {
			return script.exec(context, scope);
		} catch (RhinoException re) {
			publishRhinoException("script error during execution\n{}\n{}", re);
			throw re;
		}
		
	}

	public Object executeScriptWithContinuations(Script script, Scriptable scope) {
		assertNotClosed();
		try {
			return context.executeScriptWithContinuations(script, scope);
		} catch (RhinoException re) {
			publishRhinoException("script error during execution\n{}\n{}", re);
			throw re;
		}
	}

	public Object callFunctionWithContinuations(Callable function, Scriptable scope, Object[] args) {
		assertNotClosed();
		try {
			return context.callFunctionWithContinuations(function, scope, args);
		} catch (RhinoException re) {
			publishRhinoException("script error during function execution execution\n{}\n{}", re);
			throw re;
		}
	}

	public Object resumeContinuation(Object continuation, Scriptable scope, Object result) {
		assertNotClosed();
		try {
			return context.resumeContinuation(continuation, scope, result);
		} catch (RhinoException re) {
			publishRhinoException("script error resuming a continuation\n{}\n{}", re);
			throw re;
		}
	}
}