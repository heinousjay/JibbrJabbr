package jj.engine;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.logging.EmergencyLog;
import jj.script.RhinoContext;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;

/**
 * component that sources a prepared rhino context for script execution.
 * @author jason
 *
 */
@Singleton
class EngineAPIImpl implements EngineAPI {
	
	private final ScriptableObject global;
	
	private final EmergencyLog logger;
	
	@Inject
	EngineAPIImpl(
		final Provider<RhinoContext> contextProvider,
		final Set<HostObject> hostObjects,
		final EmergencyLog logger
	) {
		try (RhinoContext context = contextProvider.get()) {
			global = initializeGlobalScope(context, hostObjects);
			this.logger = logger;
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
					logger.error("trouble evaluating host object {} script {}", hostObject.getClass().getName(), script);
					throw new AssertionError("bad host object!", re);
				}
			}
		}

		global.sealObject();
		
		return global;
	}

}
