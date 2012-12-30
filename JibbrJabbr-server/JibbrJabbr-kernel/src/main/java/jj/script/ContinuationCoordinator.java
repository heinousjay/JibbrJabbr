package jj.script;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptStackElement;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.hostapi.RhinoObjectCreator;

/**
 * Coordinates processing a continuable script, returning the 
 * information necessary to restart the script
 * @author jason
 *
 */
class ContinuationCoordinator {
	
	private final Logger log = LoggerFactory.getLogger(ContinuationCoordinator.class);

	private final RhinoObjectCreator rhinoObjectCreator;
	
	private final CurrentScriptContext currentScriptContext;
	
	ContinuationCoordinator(
		final RhinoObjectCreator rhinoObjectCreator,
		final CurrentScriptContext currentScriptContext
	) {
		this.rhinoObjectCreator = rhinoObjectCreator;
		this.currentScriptContext = currentScriptContext;
	}
	
	/**
	 * initial execution of a page script
	 * @param scriptBundle
	 * @return true if completed, false if continued
	 */
	public ContinuationState execute(ScriptBundle scriptBundle) {
		ScriptableObject.putConstProperty(scriptBundle.scope(), "scriptKey", scriptBundle.sha1());
		try {
			rhinoObjectCreator.context().executeScriptWithContinuations(
				scriptBundle.script(), 
				scriptBundle.scope()
			);
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RhinoException re) {
			log.error("trouble executing a script {}", re.sourceName());
			log.error("{}", re.getMessage());
			for (ScriptStackElement sse : re.getScriptStack()) {
				log.error("{}", sse);
			}
		} catch (OutOfMemoryError oom) {
			throw oom;
		} catch (Throwable e) {
			log.error("unexpected problem during script execution", e);
		} finally {
			Context.exit();
		}
		return null;
	}
	
	/**
	 * function execution within the context of a page script
	 * @param scriptBundle
	 * @param functionName
	 * @param args
	 * @return true if completed, false if continued
	 */
	public ContinuationState execute(ScriptBundle scriptBundle, String functionName, Object...args) {
		
		Callable function = scriptBundle.getFunction(functionName);
		if (function != null) {	
			try {
				rhinoObjectCreator.context().callFunctionWithContinuations(function, scriptBundle.scope(), args);
			} catch (ContinuationPending continuation) {
				return extractContinuationState(continuation);
			} catch (RhinoException re) {
				log.error("trouble executing a script {}", re.sourceName());
				log.error("{}", re.getMessage());
				for (ScriptStackElement sse : re.getScriptStack()) {
					log.error("{}", sse);
				}
			} catch (OutOfMemoryError oom) {
				throw oom;
			} catch (Throwable e) {
				log.error("unexpected problem during script execution", e);
			} finally {
				Context.exit();
			}
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
	public ContinuationState resumeContinuation(final String pendingKey, final ScriptBundle scriptBundle, final Object result) {
		final ContinuationPending continuation = currentScriptContext.pendingContinuation(pendingKey);
		
		try {
			if (continuation == null) {
				throw new AssertionError("attempting to resume a non-existent continuation");
			}
			rhinoObjectCreator.context().resumeContinuation(continuation.getContinuation(), scriptBundle.scope(), result);
		} catch (ContinuationPending nextContinuation) {
			return extractContinuationState(nextContinuation);
		} catch (RhinoException re) {
			log.error("trouble executing a script {}", re.sourceName());
			log.error("{}", re.getMessage());
			for (ScriptStackElement sse : re.getScriptStack()) {
				log.error("{}", sse);
			}
		} catch (OutOfMemoryError oom) {
			throw oom;
		} catch (Throwable e) {
			log.error("unexpected problem during script execution", e);
		} finally {
			Context.exit();
		}
		return null;
	}
	
	private ContinuationState extractContinuationState(final ContinuationPending continuation) {
		final ContinuationState continuationState = (ContinuationState)continuation.getApplicationState();
		if (continuationState == null) {
			throw new AssertionError("continuation captured with no state. no need for a continuation here!");
		}
		log.debug("script continuation occurred, {}", continuationState);
		return continuationState;
	}
}
