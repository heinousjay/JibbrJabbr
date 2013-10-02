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
package jj.resource.document;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.SHA1Helper;
import jj.engine.DoCallFunction;
import jj.engine.DoInvokeFunction;
import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.IOThread;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceNotViableException;
import jj.script.FunctionContext;
import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;

/**
 * Slightly more going on than your average resource. at least some of this will
 * be moving to a base class
 * 
 * @author jason
 *
 */
@Singleton
public class DocumentScriptEnvironment extends AbstractScriptEnvironment implements FunctionContext {

	@Override
	public String baseName() {
		return baseName;
	}
	
	@Override
	public String scriptName() {
		return ScriptResourceType.Client.suffix(baseName);
	}

	@Override
	public String uri() {
		return uri;
	}

	@Override
	public String sha1() {
		return sha1;
	}
	
	@Override
	public Scriptable scope() {
		return scope;
	}

	@Override
	public Script script() {
		return script;
	}

	public Document document() {
		return html.document().clone();
	}

	@Override
	public Callable getFunction(String name) {
		return functions.get(name);
	}

	@Override
	public void addFunction(String name, Callable function) {
		functions.put(name, function);
	}

	@Override
	public boolean removeFunction(String name) {
		return functions.remove(name) != null;
	}

	@Override
	public boolean removeFunction(String name, Callable function) {
		return (functions.get(name) == function) && (functions.remove(name) == function);
	}

	@Override
	@IOThread
	protected boolean needsReplacing() throws IOException {
		// this never goes out of scope on its own
		// dependency tracking handles it all 
		return false;
	}
	
	// --- implementation
	

	private final HashMap<String, Callable> functions = new HashMap<>(4);

	private final String baseName;
	
	private final String uri;
	
	private final String sha1;
	
	private final ScriptableObject scope;
	
	private final Script script;
	
	private final HtmlResource html;
	
	private final ScriptResource clientScript;
	private final ScriptResource sharedScript;
	private final ScriptResource serverScript;
	
	/**
	 * @param cacheKey
	 */
	@Inject
	DocumentScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final String baseName,
		final ResourceFinder resourceFinder,
		final RhinoContextMaker contextMaker,
		final EngineAPI api,
		final Publisher publisher
	) {
		super(cacheKey, publisher, contextMaker, api);
		this.baseName = baseName;
		this.uri = "/" + baseName;
		
		html = resourceFinder.loadResource(HtmlResource.class, HtmlResourceCreator.resourceName(baseName));
		
		if (html == null) throw new NoSuchResourceException(baseName + "-" + baseName + ".html");
		
		clientScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Client.suffix(baseName));
		sharedScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Shared.suffix(baseName));
		serverScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
		sha1 = SHA1Helper.keyFor(
			html.sha1(),
			clientScript == null ? "none" : clientScript.sha1(),
			sharedScript == null ? "none" : sharedScript.sha1(),
			serverScript == null ? "none" : serverScript.sha1()
		);
		
		if (serverScript == null)  {
			scope = null;
			script = null;
		} else {
			scope = createLocalScope(baseName);
			
			try {
				script = compile();
			} catch (Exception e) {
				throw new ResourceNotViableException(baseName, e);
			}
		}
		
		html.addDependent(this);
		if (clientScript != null) clientScript.addDependent(this);
		if (sharedScript != null) sharedScript.addDependent(this);
		if (serverScript != null) serverScript.addDependent(this);
	}
	
	private Script compile() {
		try (RhinoContext context = contextMaker.context()) {
			
			if (sharedScript != null) {
				publisher.publish(new EvaluatingSharedScript(sharedScript.path().toString()));
				
				try {
					context.evaluateString(
						scope, 
						sharedScript.script(),
						sharedScript.path().toString()
					);
				} catch (RuntimeException e) {
					publisher.publish(new ErrorEvaluatingSharedScript(sharedScript.path().toString(), e));
					throw e;
				}
			}
			
			if (clientScript != null) {
				String clientStub = extractClientStubs();
				publisher.publish(new EvaluatingClientStub(clientScript.path().toString(), clientStub));
				try {
					context.evaluateString(scope, clientStub, "client stub for " + serverScript.path());
				} catch (RuntimeException e) {
					publisher.publish(new ErrorEvaluatingClientStub(clientScript.path().toString(), e));
					throw e;
				}
			}
		    
			publisher.publish(new CompilingServerScript(serverScript.path().toString()));
			try {
				return context.compileString(serverScript.script(), serverScript.path().toString());
			} catch (RuntimeException e) {
				publisher.publish(new ErrorCompilingServerScript(serverScript.path().toString(), e));
				throw e;
			}
		    
		}
	}
	
	private static final Pattern COUNT_PATTERN = Pattern.compile("\\r?\\n", Pattern.MULTILINE);
	
	private static final Pattern TOP_LEVEL_FUNCTION_SIGNATURE_PATTERN = 
		Pattern.compile("^function[\\s]*([^\\(]+)\\([^\\)]*\\)[\\s]*\\{[\\s]*$");
	
	private String extractClientStubs() {
		StringBuilder stubs = new StringBuilder();
		
		if (clientScript != null) {
			final String[] lines = COUNT_PATTERN.split(clientScript.script());
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
		}
		return stubs.toString();
	}
}
