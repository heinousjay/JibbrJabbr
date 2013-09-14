package jj.script;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.engine.DoCallFunction;
import jj.engine.DoInvokeFunction;
import jj.engine.EngineAPI;
import jj.execution.ScriptThread;
import jj.resource.ScriptResource;

/**
 * The factory.
 * @author jason
 *
 */
@Singleton
class ScriptExecutionEnvironmentCreator {
	
	private static final String EXPORTS = "exports";
	
	private final Logger log = LoggerFactory.getLogger(ScriptExecutionEnvironmentCreator.class);
	
	private final EngineAPI engineAPI;
	
	private final RhinoContextMaker contextMaker;
	
	@Inject
	ScriptExecutionEnvironmentCreator(final EngineAPI engineAPI, final RhinoContextMaker contextMaker) {
		this.engineAPI = engineAPI;
		this.contextMaker = contextMaker;
	}
	
	/**
	 * create a module script execution environment named by the given moduleIdentifier and
	 * associated to a parent script execution environment named by the given baseName
	 * 
	 * @param scriptResource the loaded script resource
	 * @param moduleIdentifier the resolved module identifier
	 * @param baseName the baseName of the associated script that is including
	 * this module
	 * @return
	 */
	@ScriptThread
	ModuleScriptExecutionEnvironment createScriptExecutionEnvironment(
		final ScriptResource scriptResource,
		final String moduleIdentifier,
		final String baseName
	) {
		
		log.info("creating module script execution environment for {} associated to {}", moduleIdentifier, baseName);
		log.trace("script is {}", scriptResource);
		
		Scriptable local = createLocalScope(moduleIdentifier);
		Scriptable exports;
		try (RhinoContext context = contextMaker.context()){
			exports = context.newObject(local);
			ScriptableObject.defineProperty(local, EXPORTS, exports, ScriptableObject.CONST);
		}
		
		Script script = compile(scriptResource);
		
		
		return new ModuleScriptExecutionEnvironment(scriptResource, local, script, exports, moduleIdentifier, baseName);
	}
	
	@ScriptThread
	DocumentScriptExecutionEnvironment createScriptExecutionEnvironment(
		final ScriptResource clientScriptResource,
		final ScriptResource sharedScriptResource,
		final ScriptResource serverScriptResource,
		final String baseName
	) {
		assert (serverScriptResource != null) : "null server script, nothing to do!";
		
		log.info("creating document script execution environment for {}", baseName);
		log.trace("client script is {}", clientScriptResource);
		log.trace("shared script is {}", sharedScriptResource);
		log.trace("server script is {}", serverScriptResource);
		
		String clientStubs = extractClientStubs(clientScriptResource.script());
		
		Scriptable scope = createLocalScope(baseName);
		
		Script serverScript = compile(
			scope,
			clientStubs,
			clientScriptResource,
			sharedScriptResource,
			serverScriptResource
		);
		
		return new DocumentScriptExecutionEnvironment(
			clientScriptResource,
			sharedScriptResource,
			serverScriptResource,
			scope,
			serverScript,
			baseName
		);
	}
	
	private static final Pattern COUNT_PATTERN = Pattern.compile("\\r?\\n", Pattern.MULTILINE);
	
	private static final Pattern TOP_LEVEL_FUNCTION_SIGNATURE_PATTERN = 
		Pattern.compile("^function[\\s]*([^\\(]+)\\([^\\)]*\\)[\\s]*\\{[\\s]*$");
	
	@ScriptThread
	private String extractClientStubs(String clientScript) {
		StringBuilder stubs = new StringBuilder();
		
		if (clientScript != null) {
			log.trace("extracting client stubs");
			final String[] lines = COUNT_PATTERN.split(clientScript);
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
					
					log.trace("found {}, {} return", lastMatcher.group(1), hasReturn ? "with" : "no");
					
					
					lastMatcher = null;
				}
				
				previousLine = line;
			}
		}
		return stubs.toString();
	}
	
	private Scriptable createLocalScope(final String moduleIdentifier) {
		try (RhinoContext context = contextMaker.context()) {
			Scriptable local = context.newObject(engineAPI.global());
			local.setPrototype(engineAPI.global());
		    local.setParentScope(null);
		    
		    // setting up the 'module' property as described in 
		    // the commonjs module 1.1.1 specification
		    // in the case of the top-level server script, the id
		    // will be the baseName, which fortunately happens to be
		    // exactly what is required
		    Scriptable module = context.newObject(local);
		    ScriptableObject.defineProperty(module, "id", moduleIdentifier, ScriptableObject.CONST);
		    ScriptableObject.defineProperty(local, "module", module, ScriptableObject.CONST);
		    
		    return local;
		}
	}
	
	/**
	 * compiles a set of script resources into a script, using the conventions of document processing.
	 * any exceptions are noted and rethrown to the top-level handler for the thread.
	 * @param local
	 * @param clientStub
	 * @param clientScriptResource
	 * @param sharedScriptResource
	 * @param serverScriptResource
	 * @return
	 */
	@ScriptThread
	private Script compile(
		final Scriptable local,
		final String clientStub,
		final ScriptResource clientScriptResource,
		final ScriptResource sharedScriptResource,
		final ScriptResource serverScriptResource
	) {
		try (RhinoContext context = contextMaker.context()) {
			
			if (sharedScriptResource != null) {
				context.evaluateString(
					local, 
					sharedScriptResource.script(),
					sharedScriptResource.path().toString()
				);
			}
			
			if (clientScriptResource != null) {
				try {
					log.trace("evaluating client stub");
					
					context.evaluateString(local, clientStub, "client stub for " + serverScriptResource.path());
				} catch (RuntimeException e) {
					log.error("couldn't evaluate the client stub (follows), check function definitions in {}", clientScriptResource.path());
					log.error("\n{}", clientStub);
					throw e;
				}
			}
		    
	    	log.trace("compiling server script");
	    	return context.compileString(serverScriptResource.script(), serverScriptResource.path().toString());
		    
		}
	}
	
	/**
	 * Compiles a script resource 
	 * @param local
	 * @param scriptResource
	 * @return
	 */
	@ScriptThread
	private Script compile(
		final ScriptResource scriptResource
	) {
		
		try (RhinoContext context = contextMaker.context()) {
			log.trace("compiling module script");
			return context.compileString(scriptResource.script(), scriptResource.path().toString());
		}
	}
}
