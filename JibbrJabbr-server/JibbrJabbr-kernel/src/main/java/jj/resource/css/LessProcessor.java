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
package jj.resource.css;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import jj.configuration.Configuration;
import jj.execution.ExecutionTrace;
import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;

/**
 * accepts a path that represents a less source file,
 * parses it and causes a lot of shit to happen
 * 
 * @author jason
 *
 */
@Singleton
class LessProcessor {
	
	private static final String SCRIPT_NAME = "less-1.4.0.rhino.js";
	
	private static String lessScript() throws IOException {
		
		// TODO, load from the resource system, which in turn means
		// TODO, abstract script running duties to some other object
		
		try (BufferedReader reader = 
			new BufferedReader(new InputStreamReader(LessProcessor.class.getResourceAsStream(SCRIPT_NAME), UTF_8))) {
			
			StringBuilder sb = new StringBuilder();
			String read;
			while ((read = reader.readLine()) != null) {
				sb.append(read).append('\n');
			}
			return sb.toString();
		}
	}
	
	private static class NameFunction extends BaseFunction {
		
		private static final long serialVersionUID = -1L;
		
		private static ThreadLocal<String> name = new ThreadLocal<>();
		
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length == 1) {
				name.set(String.valueOf(args[0]));
			}
			return name.get();
		}
	}
	
	private class LessOptionsFunction extends BaseFunction {
		
		private static final long serialVersionUID = -1L;
		
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			
			Scriptable object = cx.newObject(scope);
			//object.put("dumpLineNumbers", object, "all");
			//object.put("compress", object, true);
			return object;
		}
	}
	
	private class ReadFileFunction extends BaseFunction {
		
		private static final long serialVersionUID = -1L;
		
		private final Configuration configuration;
		
		ReadFileFunction(final Configuration configuration) {
			this.configuration = configuration;
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			String resourceName = String.valueOf(args[0]);
			try {
				trace.loadLessResource(resourceName);
				return new String(Files.readAllBytes(configuration.appPath().resolve(resourceName)), UTF_8);
			} catch (IOException io) {
				trace.errorLoadingLessResource(resourceName, io);
			}
			return Undefined.instance;
		}
	}

	private final ScriptableObject global;
	private final Configuration configuration;
	private final ExecutionTrace trace;
	private final RhinoContextMaker contextMaker;
	
	@Inject
	LessProcessor(
		final Configuration configuration,
		final ExecutionTrace trace,
		final RhinoContextMaker contextMaker
	) throws IOException {
		this.configuration = configuration;
		this.contextMaker = contextMaker;
		global = makeGlobal();
		this.trace = trace;
	}
	
	String process(final String baseName) {
		
		try (RhinoContext context = context(contextMaker.context())) {
			trace.startLessProcessing(baseName);
			Scriptable local = context.newObject(global);
			local.setPrototype(global);
		    local.setParentScope(null);
			Object result = context.evaluateString(local, "runLess('" + baseName + "');", baseName);
			if (result == Scriptable.NOT_FOUND) {
				return null;
			}
			return String.valueOf(result);
		} finally {
			NameFunction.name.set(null);
			trace.finishLessProcessing(baseName);
		}
	}
	
	private ScriptableObject makeGlobal() throws IOException {
		
		try (RhinoContext context = context(contextMaker.context())) {
			ScriptableObject global = context.initStandardObjects(true);
			global.defineProperty("readFile", new ReadFileFunction(configuration), ScriptableObject.EMPTY);
			global.defineProperty("name", new NameFunction(), ScriptableObject.EMPTY);
			global.defineProperty("lessOptions", new LessOptionsFunction(), ScriptableObject.EMPTY);
			context.evaluateString(global, lessScript(), SCRIPT_NAME);
			global.sealObject();
			return global;
		}
	}
	
	private RhinoContext context(RhinoContext context) {
		context.setOptimizationLevel(0);
		return context;
	}
}
