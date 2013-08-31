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

import jj.logging.EmergencyLogger;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

public class RhinoContext implements Closeable {

	private final Context context;
	private boolean closed = false;
	
	private final Logger logger;
	
	RhinoContext(final Context context, final @EmergencyLogger Logger logger) {
		this.context = context;
		this.logger = logger;
		
		context.setLanguageVersion(Context.VERSION_1_8);
		context.setOptimizationLevel(-1);
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
	
	public Scriptable newObject(Scriptable scope) {
		assertNotClosed();
		return context.newObject(scope);
	}
	
	public Function compileFunction(final Scriptable scope, final String source, final String sourceName) {
		assertNotClosed();
		try {
			return context.compileFunction(scope, source, sourceName, 1, null);
		} catch (RhinoException re) {
			logger.error("script error compiling a function\n{}", re.getMessage());
			throw re;
		}
	}

	/**
	 * @param script
	 * @param string
	 * @param i
	 * @param object
	 * @return
	 */
	public Script compileString(final String source, final String sourceName) {
		assertNotClosed();
		try {
			return context.compileString(source, sourceName, 1, null);
		} catch (RhinoException re) {
			logger.error("script error compiling a function\n{}", re.getMessage());
			throw re;
		}
	}

	/**
	 * @param configurationFunction
	 * @param scope
	 * @param scope2
	 */
	public Object callFunction(Function configurationFunction, Scriptable scope, Scriptable thisObj, Object...args) {
		assertNotClosed();
		try {
			return configurationFunction.call(context, scope, thisObj, args);
		} catch (RhinoException re) {
			logger.error("script error executing a function\n{}\n{}", re.getMessage(), re.getScriptStackTrace());
			throw re;
		}
	}

	/**
	 * @param global
	 * @param script
	 * @param scriptName
	 * @param i
	 * @param object
	 */
	public Object evaluateString(Scriptable scope, String source, String sourceName) {
		assertNotClosed();
		try {
			return context.evaluateString(scope, source, sourceName, 1, null);
		} catch (RhinoException re) {
			logger.error("script error evaluating a string\n{}\n{}", re.getMessage(), re.getScriptStackTrace());
			throw re;
		}
	}

	/**
	 * @param script
	 * @param scope
	 */
	public Object executeScriptWithContinuations(Script script, Scriptable scope) {
		assertNotClosed();
		try {
			return context.executeScriptWithContinuations(script, scope);
		} catch (RhinoException re) {
			logger.error("script error during execution\n{}\n{}", re.getMessage(), re.getScriptStackTrace());
			throw re;
		}
	}

	/**
	 * @param function
	 * @param scope
	 * @param args
	 */
	public Object callFunctionWithContinuations(Callable function, Scriptable scope, Object[] args) {
		assertNotClosed();
		try {
			return context.callFunctionWithContinuations(function, scope, args);
		} catch (RhinoException re) {
			logger.error("script error during function execution execution\n{}\n{}", re.getMessage(), re.getScriptStackTrace());
			throw re;
		}
	}

	/**
	 * @param continuation
	 * @param scope
	 * @param result
	 * @return 
	 */
	public Object resumeContinuation(Object continuation, Scriptable scope, Object result) {
		assertNotClosed();
		try {
			return context.resumeContinuation(continuation, scope, result);
		} catch (RhinoException re) {
			logger.error("script error resuming a continuation\n{}\n{}", re.getMessage(), re.getScriptStackTrace());
			throw re;
		}
	}

	/**
	 * @param i
	 */
	public void setOptimizationLevel(int i) {
		assertNotClosed();
		context.setOptimizationLevel(i);
	}
}