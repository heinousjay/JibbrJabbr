package jj.engine;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * component that sources a prepared rhino context for script execution.
 * @author jason
 *
 */
@Singleton
class EngineAPIImpl implements EngineAPI {
	
	private final ScriptableObject global;
	
	private final Logger log = LoggerFactory.getLogger(EngineAPIImpl.class);
	
	@Inject
	EngineAPIImpl(final RhinoContextMaker contextMaker, final Set<HostObject> hostObjects) {
		try (RhinoContext context = contextMaker.context()) {
			global = initializeGlobalScope(context, hostObjects);
		}
	}
	
	@Override
	public ScriptableObject global() {
		return global;
	}
	
	private ScriptableObject initializeGlobalScope(final RhinoContext context, final Set<HostObject> hostObjects) {
		final ScriptableObject global = context.initStandardObjects(true);
		// make sure all the Rhino objects are available
		context.evaluateString(global , "RegExp; getClass; java; Packages; JavaAdapter;", "lazyLoad");
		
		context.evaluateString(global, "var global=this;", "jj-internal");
		context.evaluateString(
			global,
			"global['" + PROP_CONVERT_ARGS + "'] = function(args){var out = [];Array.forEach(args,function(arg){out.push(arg);});return JSON.stringify(out);}",
			"jj-internal"
		);
		
		
		for (final HostObject hostObject : hostObjects) {
			int flags = ScriptableObject.EMPTY;
			if (hostObject.constant()) {
				flags |= ScriptableObject.CONST;
			} else {
				if (hostObject.readonly()) flags |= ScriptableObject.READONLY;
				if (hostObject.permanent()) flags |= ScriptableObject.PERMANENT;
			}
			
			if (hostObject.dontenum()) flags |= ScriptableObject.DONTENUM;
			
			global.defineProperty(hostObject.name(), hostObject, flags);
			if (hostObject instanceof ContributesScript) {
				final String script = ((ContributesScript)hostObject).script();
				try {
					context.evaluateString(global, script, hostObject.name());
				} catch (RhinoException re) {
					log.error("trouble evaluating host object {} script {}", hostObject.getClass().getName(), script);
					log.error("received exception", re);
					throw new AssertionError("bad host object!", re);
				}
			}
		}

		global.sealObject();
		
		return global;
	}

}
