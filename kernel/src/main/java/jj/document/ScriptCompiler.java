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
package jj.document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.engine.DoCallFunction;
import jj.engine.DoInvokeFunction;
import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.script.RhinoContext;
import jj.script.module.ScriptResource;

import org.mozilla.javascript.ScriptableObject;

/**
 * just a simple extraction of compilation duties from the document script resource,
 * to keep the classes relatively focused
 * 
 * 
 * @author jason
 *
 */
@Singleton
class ScriptCompiler {

	private final Provider<RhinoContext> contextProvider;
	private final Publisher publisher;
	
	@Inject
	ScriptCompiler(final Provider<RhinoContext> contextProvider, final Publisher publisher) {
		this.contextProvider = contextProvider;
		this.publisher = publisher;
	}
	

	
	void compile(
		final ScriptableObject scope,
		final ScriptResource clientScript,
		final ScriptResource sharedScript,
		final String serverScriptPath
	) {
		
		try (RhinoContext context = contextProvider.get()) {
			
			if (sharedScript != null) {
				publisher.publish(new EvaluatingSharedScript(sharedScript.path().toString()));
				
				try {
					context.evaluateString(
						scope, 
						sharedScript.source(),
						sharedScript.path().toString()
					);
				} catch (RuntimeException e) {
					publisher.publish(new ErrorEvaluatingSharedScript(sharedScript.path().toString(), e));
					throw e;
				}
			}
			
			if (clientScript != null) {
				String clientStub = extractClientStubs(clientScript);
				publisher.publish(new EvaluatingClientStub(clientScript.path().toString(), clientStub));
				try {
					context.evaluateString(scope, clientStub, "client stub for " + serverScriptPath);
				} catch (RuntimeException e) {
					publisher.publish(new ErrorEvaluatingClientStub(clientScript.path().toString(), e));
					throw e;
				}
			}
		}
	}
	
	private static final Pattern COUNT_PATTERN = Pattern.compile("\\r?\\n", Pattern.MULTILINE);
	
	private static final Pattern TOP_LEVEL_FUNCTION_SIGNATURE_PATTERN = 
		Pattern.compile("^function[\\s]*([^\\(]+)\\([^\\)]*\\)[\\s]*\\{[\\s]*$");
	
	/**
	 * Pulls the client stubs out of the client script according to some simple rules
	 * which have been detailed... somewhere.  in the test!
	 * 
	 * will throw an NPE if the client script is null
	 * @param clientScript
	 * @return
	 */
	private String extractClientStubs(final ScriptResource clientScript) {
		StringBuilder stubs = new StringBuilder();
		final String[] lines = COUNT_PATTERN.split(clientScript.source());
		Matcher lastMatcher = null;
		String previousLine = null;
		for (String line : lines) {
			if (lastMatcher == null) {
				Matcher matcher = TOP_LEVEL_FUNCTION_SIGNATURE_PATTERN.matcher(line);
				if (matcher.matches()) {
					lastMatcher = matcher;
				}
			} else if ("}".equals(line) && lastMatcher != null) {
				boolean hasReturn = previousLine.trim().startsWith("return ");
				stubs.append("function ")
					.append(lastMatcher.group(1))
					.append("(){")
					.append(hasReturn ? "return " : "")
					.append("global['")
					.append(hasReturn ? DoInvokeFunction.PROP_DO_INVOKE : DoCallFunction.PROP_DO_CALL)
					.append("']('")
					.append(lastMatcher.group(1))
					.append("',global['")
					.append(EngineAPI.PROP_CONVERT_ARGS)
					.append("'](arguments))")
					.append(";}\n");
				
				//log.trace("found {}, {} return", lastMatcher.group(1), hasReturn ? "with" : "no");
				
				
				lastMatcher = null;
			}
			
			previousLine = line;
		}
		return stubs.toString();
	}
}
