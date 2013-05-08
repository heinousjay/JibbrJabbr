package jj.hostapi;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class RhinoObjectCreatorImpl implements RhinoObjectCreator {
	
	private static final class JJContext extends Context {
		JJContext(final ContextFactory factory) {
			super(factory);
		}
	}
	
	private static final class JJContextFactory extends ContextFactory {
		
		@Override
		protected Context makeContext() {
			Context context = new JJContext(this);
			// always in interpreter mode for continuation support
			context.setOptimizationLevel(-1);
			// this should be configurable? maybe not
			context.setLanguageVersion(Context.VERSION_1_8);
			return context;
		}
		
		@Override
		protected boolean hasFeature(Context cx, int featureIndex) {
			return (featureIndex == Context.FEATURE_ENHANCED_JAVA_ACCESS) || super.hasFeature(cx, featureIndex);
		}
	}
	
	static {
		ContextFactory.initGlobal(new JJContextFactory());
	}
	
	private final ScriptableObject global;
	
	private final Logger log = LoggerFactory.getLogger(RhinoObjectCreatorImpl.class);
	
	@Inject
	RhinoObjectCreatorImpl(final Set<HostObject> hostObjects) {
		Context context = Context.enter();
		try {
			global = initializeGlobalScope(context, hostObjects);
		} finally {
			Context.exit();
		}
	}

	public Context context() {
		return Context.enter();
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
