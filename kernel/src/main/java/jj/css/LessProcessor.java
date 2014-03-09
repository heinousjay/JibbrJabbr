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
package jj.css;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import jj.configuration.Application;
import jj.event.Publisher;
import jj.script.RhinoContext;

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
		
		// TODO, LessScriptEnvironment
		
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
		
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			String resourceName = String.valueOf(args[0]);
			try {
				publisher.publish(new LoadLessResource(resourceName));
				return new String(Files.readAllBytes(app.path().resolve(resourceName)), UTF_8);
			} catch (IOException io) {
				publisher.publish(new ErrorLoadingLessResource(resourceName, io));
			}
			return Undefined.instance;
		}
	}

	private final ScriptableObject global;
	private final Application app;
	private final Provider<RhinoContext> contextProvider;
	private final Publisher publisher;
	
	@Inject
	LessProcessor(
		final Application app,
		final Provider<RhinoContext> contextProvider,
		final Publisher publisher
	) throws IOException {
		this.app = app;
		this.contextProvider = contextProvider;
		this.publisher = publisher;
		global = makeGlobal();
	}
	
	String process(final String lessName) {

		publisher.publish(new StartingLessProcessing(lessName));
		try (RhinoContext context = contextProvider.get().withoutContinuations()) {
			
			Scriptable local = context.newObject(global);
			local.setPrototype(global);
			local.setParentScope(null);
			Object result = context.evaluateString(local, "runLess('" + lessName + "');", lessName);
			
			if (result == Scriptable.NOT_FOUND) {
				return null;
			}
			
			return String.valueOf(result);
			
		} finally {
			NameFunction.name.set(null);
			publisher.publish(new FinishedLessProcessing(lessName));
		}
	}
	
	private ScriptableObject makeGlobal() throws IOException {
		
		try (RhinoContext context = contextProvider.get().withoutContinuations()) {
			ScriptableObject global = context.initStandardObjects(true);
			global.defineProperty("readFile", new ReadFileFunction(), ScriptableObject.EMPTY);
			global.defineProperty("name", new NameFunction(), ScriptableObject.EMPTY);
			global.defineProperty("lessOptions", new LessOptionsFunction(), ScriptableObject.EMPTY);
			context.evaluateString(global, lessScript(), SCRIPT_NAME);
			global.sealObject();
			return global;
		}
	}
}