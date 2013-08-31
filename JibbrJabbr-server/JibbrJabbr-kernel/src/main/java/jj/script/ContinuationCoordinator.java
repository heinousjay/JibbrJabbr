package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

import jj.logging.EmergencyLogger;

/**
 * Coordinates processing a continuable script, returning the 
 * information necessary to restart the script
 * @author jason
 *
 */
@Singleton
class ContinuationCoordinator {
	
	private interface ContinuationExecution {
		void run(RhinoContext context);
	}
	
	private final Logger log;
	
	private final RhinoContextMaker contextMaker;
	
	private final CurrentScriptContext currentScriptContext;
	
	@Inject
	ContinuationCoordinator(
		final RhinoContextMaker contextMaker,
		final CurrentScriptContext currentScriptContext,
		final @EmergencyLogger Logger log
	) {
		this.contextMaker = contextMaker;
		this.currentScriptContext = currentScriptContext;
		this.log = log;
	}
	
	private void log(final Exception e, final ScriptBundle scriptBundle) {
		log.error("unexpected problem during script execution {}", scriptBundle);
		log.error("", e);
	} 
	
	private ContinuationState execute(final ContinuationExecution execution, final ScriptBundle scriptBundle) {
		try (RhinoContext context = contextMaker.context()){
			execution.run(context);
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RuntimeException e) {
			log(e, scriptBundle);
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
			public void run(RhinoContext context) {
				context.executeScriptWithContinuations(scriptBundle.script(), scriptBundle.scope());
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
				public void run(RhinoContext context) {
					context.callFunctionWithContinuations(function, scriptBundle.scope(), args);
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
				public void run(RhinoContext context) {
					context.resumeContinuation(continuation.getContinuation(), scriptBundle.scope(), result);
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
