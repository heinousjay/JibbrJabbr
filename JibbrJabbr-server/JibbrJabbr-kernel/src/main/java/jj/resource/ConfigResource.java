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
package jj.resource;

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

import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;
import jj.script.Util;

/**
 * @author jason
 *
 */
@Singleton
public class ConfigResource extends AbstractFileResource {

	public static final String CONFIG_JS = "config.js";
	
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
		try (RhinoContext context = contextMaker.context()) {
			
			global = context.initStandardObjects();
			Function configurationFunction = context.compileFunction(global, script(), path.normalize().toString());
			Object mapCandidate = context.callFunction(configurationFunction, global, global);
			
			if (!(mapCandidate instanceof Map)) {
				throw new ResourceNotViableException(path, "configuration function did not return the right object");
			}
			
			configFunctions = castAndVerify(mapCandidate);
		} catch (IllegalArgumentException iae) {
			throw new ResourceNotViableException(path, CONFIG_JS + " is expected to be a function.");
		} catch (RhinoException re) {
			throw new ResourceNotViableException(path, re);
		}
		
	}
	
	private Map<String, Function> castAndVerify(Object mapIn) {
		@SuppressWarnings("rawtypes")
		Map map = (Map)mapIn;
		Map<String, Function> configFunctions = new HashMap<>();
		for (Object keyObj : map.keySet()) {
			String key = Util.toJavaString(keyObj);
			if (key.isEmpty()) throw new ResourceNotViableException(path, "bad key");
			Object valueObj = map.get(keyObj);
			if (!(valueObj instanceof Function)) throw new ResourceNotViableException(path, "not a function");
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
