package jj.hostapi;

import static jj.hostapi.MIME.*;
import static jj.hostapi.Method.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
/**
 * takes a rhino map of properties and merges them against
 * defaults and exposes everything in a typed manner.
 *
 * @author jason
 *
 */
class RestCallOptions {

	private static final String PATH = "path";
	private static final String METHOD = "method";
	private static final String ACCEPT = "accept";
	private static final String PRODUCE = "produce";
	private static final String PARAMS = "params";
	private static final String IGNORE_RESULT = "ignoreResult";
	
	private static final String DEFAULT_PATH = "";
	private static final Method DEFAULT_METHOD = GET;
	private static final MIME DEFAULT_ACCEPT = JSON;
	private static final Scriptable DEFAULT_PARAMS = null;
	private static final boolean DEFAULT_IGNORE_RESULT = false;
	
	private final String path;
	private final Method method;
	private final MIME accept;
	private final MIME produce;
	private final Scriptable params;
	private final boolean ignoreResult;
	
	RestCallOptions(final Scriptable options) {
		
		path = path(options);
		method = method(options);
		accept = accept(options);
		produce = produce(options);
		params = params(options);
		ignoreResult = ignoreResult(options);
	}

	private String path(final Scriptable options) {
		return options != null && ScriptableObject.hasProperty(options, PATH) ?
			toJavaString(options, PATH) :
			DEFAULT_PATH;
	}
	
	private Method method(final Scriptable options) {
		return options != null && ScriptableObject.hasProperty(options, METHOD) ?
			Method.valueOf(toJavaString(options, METHOD)) :
			DEFAULT_METHOD;
	}
	
	private MIME accept(final Scriptable options) {
		return options != null && ScriptableObject.hasProperty(options, ACCEPT) ?
			MIME.valueOf(toJavaString(options, ACCEPT)) :
			DEFAULT_ACCEPT;
	}
	
	private MIME produce(final Scriptable options) {
		return options != null && ScriptableObject.hasProperty(options, PRODUCE) ?
			MIME.valueOf(toJavaString(options, PRODUCE)) :
			method(options).produces();
	}
	
	private Scriptable params(final Scriptable options) {
		Scriptable result = DEFAULT_PARAMS;
		if (options != null && ScriptableObject.hasProperty(options, PARAMS)) {
			Object candidate = ScriptableObject.getProperty(options, PARAMS);
			
			if (candidate != null && !(candidate instanceof Scriptable)) {
				throw new IllegalArgumentException("params must be an object");
			}
			if (candidate != null) {
				result = (Scriptable)candidate;
			}
		}
		return result;
	}
	
	private boolean ignoreResult(final Scriptable options) {
		return options != null && ScriptableObject.hasProperty(options, IGNORE_RESULT) ?
			toJavaBoolean(options, IGNORE_RESULT) :
			DEFAULT_IGNORE_RESULT;
	}
	
	private String toJavaString(final Scriptable options, final String key) {
		try {
			return (String)Context.jsToJava(ScriptableObject.getProperty(options, key), String.class);
		} catch (EvaluatorException e) {
			throw new IllegalArgumentException(key + " must be a string");
		}
	}
	
	private boolean toJavaBoolean(final Scriptable options, final String key) {
		try {
		return (boolean)Context.jsToJava(ScriptableObject.getProperty(options, key), Boolean.TYPE);
		} catch (Exception e) {
			throw new IllegalArgumentException(key + " must be a boolean");
		}
	}
	
	String path() {
		return path;
	}
	
	Method method() {
		return method;
	}
	
	MIME accept() {
		return accept;
	}
	
	MIME produce() {
		return produce;
	}
	
	Scriptable params() {
		return params;
	}
	
	boolean ignoreResult() {
		return ignoreResult;
	}
}
