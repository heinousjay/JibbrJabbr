package jj.script;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.ScriptThread;
import jj.hostapi.DoCallFunction;
import jj.hostapi.DoInvokeFunction;
import jj.hostapi.RhinoObjectCreator;
import jj.resource.ScriptResource;

/**
 * responsible for creating a script bundle, which includes compiling the
 * script in the host environment
 * @author jason
 *
 */
class ScriptBundleCreator {
	
	private final Logger log = LoggerFactory.getLogger(ScriptBundleCreator.class);
	
	private final RhinoObjectCreator rhinoObjectCreator;

	ScriptBundleCreator(final RhinoObjectCreator rhinoObjectCreator) {
		this.rhinoObjectCreator = rhinoObjectCreator;
	}
	
	@ScriptThread
	AssociatedScriptBundle createScriptBundle(
		final ScriptResource clientScriptResource,
		final ScriptResource sharedScriptResource,
		final ScriptResource serverScriptResource,
		final String baseName
	) {
		if (serverScriptResource == null) {
			throw new IllegalArgumentException("null server script, nothing to do!");
		}
		
		log.debug("creating script bundle");
		
		String clientStubs = extractStubs(clientScriptResource != null ? clientScriptResource.script() : "");
		Scriptable scope = createLocalScope();
		Script serverScript = 
			compile(scope, clientStubs, clientScriptResource, sharedScriptResource, serverScriptResource);
		
		return new AssociatedScriptBundle(
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
	private String extractStubs(String clientScript) {
		// keyed signature to whether this needs a return (ie continuation)
		// this will be global and named better of course
		StringBuilder stubs = new StringBuilder();
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
					.append(hasReturn ? "return JSON.parse(" : "")
					.append("global['")
					.append(hasReturn ? DoInvokeFunction.PROP_DO_INVOKE : DoCallFunction.PROP_DO_CALL)
					.append("']('")
					.append(lastMatcher.group(1))
					.append("',global['")
					.append(RhinoObjectCreator.PROP_CONVERT_ARGS)
					.append("'](arguments))")
					.append(hasReturn ? ")" : "")
					.append(";}\n");
				
				
				lastMatcher = null;
			}
			
			previousLine = line;
		}
		
		return stubs.toString();
	}
	
	@ScriptThread
	private Scriptable createLocalScope() {
		Context context = rhinoObjectCreator.context();
		try { 
			Scriptable local = context.newObject(rhinoObjectCreator.global());
			local.setPrototype(rhinoObjectCreator.global());
		    local.setParentScope(null);
		    
		    return local;
		} finally {
			Context.exit();
		}
	}
	
	/**
	 * compiles a set of scripts into a bundle.  any exceptions are noted and rethrown to
	 * the top-level handler for the thread
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
		Context context = rhinoObjectCreator.context();
		try { 
			
			if (sharedScriptResource != null) {
				try {
					log.debug("evaluating shared script");
					
					context.evaluateString(
						local, 
						sharedScriptResource.script(),
						sharedScriptResource.path().toString(),
						1,
						null
					);
				} catch (Exception e) {
					log.error("couldn't evaluate {}", sharedScriptResource.path());
					throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
				}
			}
			
			if (clientScriptResource != null) {
				try {
					log.debug("evaluating client stub");
					
					context.evaluateString(local, clientStub, "client stub for " + serverScriptResource.path(), 1, null);
				} catch (Exception e) {
					log.error("couldn't evaluate the client stub, check function definitions in {}", clientScriptResource.path());
					throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
				}
			}
		    
	    	log.debug("compiling server script");
	    	return context.compileString(
	    		serverScriptResource.script(),
	    		serverScriptResource.path().toString(),
	    		1,
	    		null
	    	);
		    
		} finally {
			Context.exit();
		}
	}
}
