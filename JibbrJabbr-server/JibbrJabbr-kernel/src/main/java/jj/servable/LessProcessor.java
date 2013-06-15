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
package jj.servable;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.Configuration;

/**
 * accepts a path that represents a less source file,
 * parses it and causes a lot of shit to happen
 * 
 * @author jason
 *
 */
class LessProcessor {
	
	private static final String SCRIPT_NAME = "less-1.4.0.rhino.js";
	
	private static String lessScript() throws IOException {
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
	
	private static class ReadFileFunction extends BaseFunction {
		
		private static final long serialVersionUID = -1L;
		
		private final Path basePath;
		
		ReadFileFunction(final Path basePath) {
			this.basePath = basePath;
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				return new String(Files.readAllBytes(basePath.resolve(String.valueOf(args[0]))), UTF_8);
			} catch (IOException io) {
				
			}
			
			return Scriptable.NOT_FOUND;
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

	private final ScriptableObject global;
	private final Configuration configuration;
	
	LessProcessor(
		final Configuration configuration
	) throws IOException {
		this.configuration = configuration;
		global = makeGlobal();
	}
	
	String process(final String baseName) {
		Path path = configuration.basePath().resolve(baseName);
		
		Context context = context();
		try {
			Scriptable local = context.newObject(global);
			local.setPrototype(global);
		    local.setParentScope(null);
			Object result = context.evaluateString(local, "runLess('" + path.getFileName() + "');", baseName, 1, null);
			if (result == Scriptable.NOT_FOUND) {
				return null;
			}
			return String.valueOf(result);
		} finally {
			Context.exit();
			NameFunction.name.set(null);
		}
	}
	
	private ScriptableObject makeGlobal() throws IOException {
		Context context = context();
		try {
			ScriptableObject global = context.initStandardObjects(null, true);
			global.defineProperty("readFile", new ReadFileFunction(configuration.basePath()), ScriptableObject.EMPTY);
			global.defineProperty("name", new NameFunction(), ScriptableObject.EMPTY);
			global.defineProperty("lessOptions", new LessOptionsFunction(), ScriptableObject.EMPTY);
			context.evaluateString(global, lessScript(), SCRIPT_NAME, 1, null);
			global.sealObject();
			return global;
		} finally {
			Context.exit();
		}
	}
	
	private Context context() {
		Context context = Context.enter();
		context.setOptimizationLevel(0);
		context.setLanguageVersion(Context.VERSION_1_8);
		return context;
	}
}
