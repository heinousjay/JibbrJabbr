package jj.engine;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Context;
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
	EngineAPIImpl(final Set<HostObject> hostObjects) {
		Context context = context();
		try {
			global = initializeGlobalScope(context, hostObjects);
		} finally {
			Context.exit();
		}
	}

	public Context context() {
		Context context = Context.enter();
		// always in interpreter mode for continuation support
		context.setOptimizationLevel(-1);
		// this should be configurable? maybe not
		context.setLanguageVersion(Context.VERSION_1_8);
		return context;
	}

	public ScriptableObject global() {
		return global;
	}
	
	private ScriptableObject initializeGlobalScope(final Context context, final Set<HostObject> hostObjects) {
		final ScriptableObject global = context.initStandardObjects(null, true);
		// make sure all the Rhino objects are available
		context.evaluateString(global , "RegExp; getClass; java; Packages; JavaAdapter;", "lazyLoad", 0, null);
		
		context.evaluateString(global, "var global=this;", "jj-internal", 0, null);
		context.evaluateString(
			global, 
			"global['" + PROP_CONVERT_ARGS + "'] = function(args){var out = [];Array.forEach(args,function(arg){out.push(arg);});return JSON.stringify(out);}", 
			"jj-internal", 
			0, 
			null
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
					context.evaluateString(global, script, hostObject.name(), 1, null);
				} catch (RhinoException re) {
					log.error("trouble evaluating host object {} script {}", hostObject.getClass().getName(), script);
					log.error("received exception", re);
				}
			}
		}

		global.sealObject();
		
		return global;
	}

}