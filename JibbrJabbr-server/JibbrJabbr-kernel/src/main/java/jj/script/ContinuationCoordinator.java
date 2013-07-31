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

import jj.engine.EngineAPI;
import jj.logging.EmergencyLogger;

/**
 * Coordinates processing a continuable script, returning the 
 * information necessary to restart the script
 * @author jason
 *
 */
@Singleton
class ContinuationCoordinator {
	
	private interface ContinuationExecution extends Runnable {}
	
	private final Logger log;

	private final EngineAPI engineAPI;
	
	private final CurrentScriptContext currentScriptContext;
	
	@Inject
	ContinuationCoordinator(
		final EngineAPI engineAPI,
		final CurrentScriptContext currentScriptContext,
		final @EmergencyLogger Logger log
	) {
		this.engineAPI = engineAPI;
		this.currentScriptContext = currentScriptContext;
		this.log = log;
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
	
	private ContinuationState execute(final ContinuationExecution execution, final ScriptBundle scriptBundle) {
		try {
			execution.run();
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RhinoException re) {
			log(re);
		} catch (RuntimeException e) {
			log(e, scriptBundle);
		} finally {
			Context.exit();
		}
		return null;
	}
	
	/**
	 * initial execution of a script, either associated to a document, or a required module
	 * @param scriptBundle
	 * @return true if completed, false if continued
	 */
	ContinuationState execute(final ScriptBundle scriptBundle) {
		
		assert (scriptBundle != null) : "cannot execute without a script bundle";
		
		ScriptableObject.putConstProperty(scriptBundle.scope(), "scriptKey", scriptBundle.sha1());
		
		return execute(new ContinuationExecution() {
			
			@Override
			public void run() {
				engineAPI.context().executeScriptWithContinuations(scriptBundle.script(), scriptBundle.scope());
			}
		}, scriptBundle);
	}
	
	/**
	 * function execution within the context of script associated to a document
	 * @param scriptBundle
	 * @param functionName
	 * @param args
	 * @return true if completed, false if continued
	 */
	ContinuationState execute(final AssociatedScriptBundle scriptBundle, final Callable function, final Object...args) {
		
		assert (scriptBundle != null) : "cannot execute without a script bundle";
		
		if (function != null) {

			return execute(new ContinuationExecution() {
				
				@Override
				public void run() {
					engineAPI.context().callFunctionWithContinuations(function, scriptBundle.scope(), args);
				}
			}, scriptBundle);
			
		}
		
		log.error("ignoring attempt to execute nonexistent function in context of {}", scriptBundle);
		log.error("helpful stacktrace", new Exception());
		return null;
	}
	
	/**
	 * Resumes a continuation that was previously saved from an execution in this class
	 * @param pendingKey
	 * @param scriptBundle
	 * @param result
	 * @return
	 */
	ContinuationState resumeContinuation(final String pendingKey, final ScriptBundle scriptBundle, final Object result) {
		
		assert (scriptBundle != null) : "cannot resume without a script bundle";
		
		final ContinuationPending continuation = currentScriptContext.pendingContinuation(pendingKey);
		if (continuation != null) {

			return execute(new ContinuationExecution() {
				
				@Override
				public void run() {
					engineAPI.context().resumeContinuation(continuation.getContinuation(), scriptBundle.scope(), result);
				}
			}, scriptBundle);
		}
		
		log.error("attempting to resume a non-existent continuation in {} keyed by {}", scriptBundle, pendingKey);
		log.error("helpful stacktrace", new Exception());
		return null;
	}
	
	private ContinuationState extractContinuationState(final ContinuationPending continuation) {
		
		final ContinuationState continuationState = (ContinuationState)continuation.getApplicationState();
		assert (continuationState != null) : "continuation captured with no state";
		
		return continuationState;
	}
}
