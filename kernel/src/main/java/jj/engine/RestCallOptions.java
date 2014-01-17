package jj.engine;

import static jj.engine.MIME.*;

import io.netty.handler.codec.http.HttpMethod;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	
	private static final Map<HttpMethod, MIME> PRODUCTION_DEFAULTS;
	
	static {
		HashMap<HttpMethod, MIME> worker = new HashMap<>();
		worker.put(HttpMethod.GET, UrlEncoded);
		worker.put(HttpMethod.POST, JSON);
		worker.put(HttpMethod.PUT, JSON);
		worker.put(HttpMethod.DELETE, UrlEncoded);
		
		PRODUCTION_DEFAULTS = Collections.unmodifiableMap(worker);
	}

	private static final String PATH = "path";
	private static final String METHOD = "method";
	private static final String ACCEPT = "accept";
	private static final String PRODUCE = "produce";
	private static final String PARAMS = "params";
	private static final String IGNORE_RESULT = "ignoreResult";
	
	private static final String DEFAULT_PATH = "";
	private static final HttpMethod DEFAULT_METHOD = HttpMethod.GET;
	private static final MIME DEFAULT_ACCEPT = JSON;
	private static final Map<String, Object> DEFAULT_PARAMS = null;
	private static final boolean DEFAULT_IGNORE_RESULT = false;
	
	private final String path;
	private final HttpMethod method;
	private final MIME accept;
	private final MIME produce;
	private final Map<String, Object> params;
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
	
	private HttpMethod method(final Scriptable options) {
		return options != null && ScriptableObject.hasProperty(options, METHOD) ?
			HttpMethod.valueOf(toJavaString(options, METHOD)) :
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
			PRODUCTION_DEFAULTS.get(method(options));
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> params(final Scriptable options) {
		Map<String, Object> result = DEFAULT_PARAMS;
		if (options != null && ScriptableObject.hasProperty(options, PARAMS)) {
			Object candidate = ScriptableObject.getProperty(options, PARAMS);
			
			if (candidate != null && !(candidate instanceof Map<?, ?>)) {
				throw new IllegalArgumentException("params must be an object");
			}
			if (candidate != null) {
				result = (Map<String, Object>)candidate;
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
	
	HttpMethod method() {
		return method;
	}
	
	MIME accept() {
		return accept;
	}
	
	MIME produce() {
		return produce;
	}
	
	Map<String, Object> params() {
		return params;
	}
	
	boolean ignoreResult() {
		return ignoreResult;
	}
}
