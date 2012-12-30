package jj.hostapi;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RhinoObjectCreatorImpl implements RhinoObjectCreator {
	
	private static Context contextInner() {
		Context context = Context.enter();
		// always in interpreter mode for continuation support
		context.setOptimizationLevel(-1);
		// this should be configurable? maybe not
		context.setLanguageVersion(Context.VERSION_1_8);
		return context;
	}
	
	private final ScriptableObject global;
	
	private final Logger log = LoggerFactory.getLogger(RhinoObjectCreatorImpl.class);
	
	RhinoObjectCreatorImpl(final HostObject[] hostObjects) {
		Context context = contextInner();
		try {
			global = initializeGlobalScope(context, hostObjects);
		} finally {
			Context.exit();
		}
	}

	public Context context() {
		return contextInner();
	}

	public ScriptableObject global() {
		return global;
	}
	
	private ScriptableObject initializeGlobalScope(final Context context, final HostObject[] hostObjects) {
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
