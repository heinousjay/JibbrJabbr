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
package jj.resource.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

import jj.resource.AbstractFileResource;
import jj.resource.MimeTypes;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceNotViableException;
import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;
import jj.script.Util;

/**
 * @author jason
 *
 */
@Singleton
public class ConfigResource extends AbstractFileResource {

	private static final String BAD_RETURN = "configuration function did not return the right object";
	private static final String BAD_RETURN_NOINIT = "configuration function did not return the right object. RESTART THE SERVER.";

	public static final String CONFIG_JS = "config.js";

	private static final String DID_NOT_EXECUTE = CONFIG_JS + " did not execute.";
	private static final String DID_NOT_EXECUTE_NO_INIT = CONFIG_JS + " did not execute. RESTART THE SERVER.";

	private static final String DID_NOT_COMPILE = CONFIG_JS + " did not compile.";
	private static final String DID_NOT_COMPILE_NO_INIT = CONFIG_JS + " did not compile. RESTART THE SERVER.";
	
	private static final String NOT_A_FUNCTION = "%s is expected to be a function.";
	private static final String NOT_A_FUNCTION_NO_INIT = "%s is expected to be a function. RESTART THE SERVER.";
	
	private static boolean initialized = false;
	
	private final Scriptable global;
	
	private final Map<String, Function> configFunctions;
	
	/**
	 * @param baseName
	 * @param path
	 * @throws IOException
	 */
	@Inject
	ConfigResource(
		final RhinoContextMaker contextMaker,
		final ResourceCacheKey cacheKey,
		final Path path
	) {
		super(cacheKey, CONFIG_JS, path);
		Function configurationFunction;
		try (RhinoContext context = contextMaker.context()) {
			
			global = context.initStandardObjects();
			configurationFunction = context.compileFunction(global, script(), path.normalize().toString());
			
		} catch (IllegalArgumentException iae) {
			throw new ResourceNotViableException(path, String.format(initialized ? NOT_A_FUNCTION : NOT_A_FUNCTION_NO_INIT, CONFIG_JS));
		} catch (RhinoException re) {
			throw new ResourceNotViableException(path, initialized ? DID_NOT_COMPILE : DID_NOT_COMPILE_NO_INIT);
		}
		
		Object mapCandidate;
		try (RhinoContext context = contextMaker.context()) {
			mapCandidate = context.callFunction(configurationFunction, global, global);
		} catch (RhinoException re) {
			throw new ResourceNotViableException(path, initialized ? DID_NOT_EXECUTE : DID_NOT_EXECUTE_NO_INIT);
		}
		
		if (!(mapCandidate instanceof Map)) {
			throw new ResourceNotViableException(path, initialized ? BAD_RETURN : BAD_RETURN_NOINIT);
		}
		
		configFunctions = castAndVerify(mapCandidate);
		
		initialized = true;
	}
	
	private Map<String, Function> castAndVerify(Object mapIn) {
		@SuppressWarnings("rawtypes")
		Map map = (Map)mapIn;
		Map<String, Function> configFunctions = new HashMap<>();
		for (Object keyObj : map.keySet()) {
			String key = Util.toJavaString(keyObj);
			if (key.isEmpty()) throw new ResourceNotViableException(path, "bad key");
			Object valueObj = map.get(keyObj);
			if (!(valueObj instanceof Function)) {
				
				throw new ResourceNotViableException(path, String.format(initialized ? NOT_A_FUNCTION : NOT_A_FUNCTION_NO_INIT, key));
			}
			configFunctions.put(key, (Function)valueObj);
		}
		return Collections.unmodifiableMap(configFunctions);
	}

	@Override
	public String uri() {
		// no serving this!
		return "/";
	}

	@Override
	public String mime() {
		return MimeTypes.getDefault();
	}
	
	public final Map<String, Function> functions() {
		return configFunctions;
	}
	
	public Scriptable global() {
		return global;
	}
	
	private String script() {
		return byteBuffer.toString(UTF_8);
	}
}
