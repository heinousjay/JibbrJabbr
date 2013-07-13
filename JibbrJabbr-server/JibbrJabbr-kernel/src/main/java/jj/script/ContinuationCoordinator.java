package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptStackElement;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.engine.EngineAPI;

/**
 * Coordinates processing a continuable script, returning the 
 * information necessary to restart the script
 * @author jason
 *
 */
@Singleton
class ContinuationCoordinator {
	
	private final Logger log = LoggerFactory.getLogger(ContinuationCoordinator.class);

	private final EngineAPI rhinoObjectCreator;
	
	private final CurrentScriptContext currentScriptContext;
	
	@Inject
	ContinuationCoordinator(
		final EngineAPI rhinoObjectCreator,
		final CurrentScriptContext currentScriptContext
	) {
		this.rhinoObjectCreator = rhinoObjectCreator;
		this.currentScriptContext = currentScriptContext;
	}
	
	private void log(final RhinoException re) {
		if (log.isErrorEnabled()) {
			StringBuilder sb = new StringBuilder("trouble executing a script")
				.append(re.sourceName())
				.append("\n============== BEGIN SCRIPT TRACE ==============\n")
				.append(re.getMessage());
			for (ScriptStackElement sse : re.getScriptStack()) {
				sb.append('\n').append(sse);
			}
			sb.append("\n==============  END SCRIPT TRACE  ==============\n");
			log.error("{}", sb);
		}
	}
	
	private void log(final Exception e, final ScriptBundle scriptBundle) {
		log.error("unexpected problem during script execution {}", scriptBundle);
		log.error("", e);
	} 
	/**
	 * initial execution of a script
	 * @param scriptBundle
	 * @return true if completed, false if continued
	 */
	ContinuationState execute(ScriptBundle scriptBundle) {
		
		assert (scriptBundle != null) : "cannot execute without a script bundle";
		
		log.trace("executing {}", scriptBundle);
		
		ScriptableObject.putConstProperty(scriptBundle.scope(), "scriptKey", scriptBundle.sha1());
		try {
			rhinoObjectCreator.context().executeScriptWithContinuations(
				scriptBundle.script(), 
				scriptBundle.scope()
			);
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RhinoException re) {
			log(re);
		} catch (Exception e) {
			log(e, scriptBundle);
		} finally {
			Context.exit();
		}
		return null;
	}
	
	/**
	 * function execution within the context of an html associated script
	 * @param scriptBundle
	 * @param functionName
	 * @param args
	 * @return true if completed, false if continued
	 */
	ContinuationState execute(AssociatedScriptBundle scriptBundle, Callable function, Object...args) {
		
		assert (scriptBundle != null) : "cannot execute without a script bundle";
		if (function != null) {
			
			
			try {
				rhinoObjectCreator.context().callFunctionWithContinuations(function, scriptBundle.scope(), args);
			} catch (ContinuationPending continuation) {
				return extractContinuationState(continuation);
			} catch (RhinoException re) {
				log(re);
			} catch (Exception e) {
				log(e, scriptBundle);
			} finally {
				Context.exit();
			}	
		} else {
			log.warn("ignoring attempt to execute nonexistent function in context of {}", scriptBundle);
		}
		
		return null;
	}
	
	/**
	 * Resumes a continuation for the current context.
	 * @param pendingKey
	 * @param scriptBundle
	 * @param result
	 * @return
	 */
	ContinuationState resumeContinuation(final String pendingKey, final ScriptBundle scriptBundle, final Object result) {
		final ContinuationPending continuation = currentScriptContext.pendingContinuation(pendingKey);
		assert (continuation != null) : ("attempting to resume a non-existent continuation at " + pendingKey + " in " + scriptBundle);
		
		log.trace("resuming continuation in {} with key {} and result {}", scriptBundle, pendingKey, result);
		
		try {
			rhinoObjectCreator.context().resumeContinuation(continuation.getContinuation(), scriptBundle.scope(), result);
		} catch (ContinuationPending nextContinuation) {
			return extractContinuationState(nextContinuation);
		} catch (RhinoException re) {
			log(re);
		} catch (Exception e) {
			log(e, scriptBundle);
		} finally {
			Context.exit();
		}
		return null;
	}
	
	private ContinuationState extractContinuationState(final ContinuationPending continuation) {
		
		final ContinuationState continuationState = (ContinuationState)continuation.getApplicationState();
		assert (continuationState != null) : "continuation captured with no state";

		log.trace("script continuation captured, {}", continuationState);
		return continuationState;
	}
}
