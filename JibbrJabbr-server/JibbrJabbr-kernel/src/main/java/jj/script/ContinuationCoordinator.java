package jj.script;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

import jj.logging.EmergencyLogger;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ScriptEnvironment;

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
	
	private final Map<ContinuationType, ContinuationProcessor> continuationProcessors;
	
	@Inject
	ContinuationCoordinator(
		final RhinoContextMaker contextMaker,
		final CurrentScriptContext currentScriptContext,
		final @EmergencyLogger Logger log,
		final Map<ContinuationType, ContinuationProcessor> continuationProcessors
	) {
		this.contextMaker = contextMaker;
		this.currentScriptContext = currentScriptContext;
		this.log = log;
		this.continuationProcessors = continuationProcessors;
	}
	
	private void log(final Exception e, final ScriptEnvironment scriptEnvironment) {
		log.error("unexpected problem during script execution {}", scriptEnvironment);
		log.error("", e);
	} 
	
	private ContinuationState execute(final ContinuationExecution execution, final ScriptEnvironment scriptEnvironment) {
		try (RhinoContext context = contextMaker.context()){
			execution.run(context);
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RuntimeException e) {
			log(e, scriptEnvironment);
		}
		return null;
	}
	
	/**
	 * initial execution of a script, either associated to a document, or a required module
	 * @param scriptEnvironment
	 * @return true if completed, false if continued
	 */
	boolean execute(final ScriptEnvironment scriptEnvironment) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		ScriptableObject.putConstProperty(scriptEnvironment.scope(), "scriptKey", scriptEnvironment.sha1());
		
		return processContinuationState(execute(new ContinuationExecution() {
			
			@Override
			public void run(RhinoContext context) {
				context.executeScriptWithContinuations(scriptEnvironment.script(), scriptEnvironment.scope());
			}
		}, scriptEnvironment));
	}
	
	/**
	 * function execution within the context of script associated to a document
	 * @param scriptEnvironment
	 * @param functionName
	 * @param args
	 * @return true if completed, false if continued
	 */
	boolean execute(final DocumentScriptEnvironment scriptEnvironment, final Callable function, final Object...args) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		if (function != null) {

			return processContinuationState(execute(new ContinuationExecution() {
				
				@Override
				public void run(RhinoContext context) {
					context.callFunctionWithContinuations(function, scriptEnvironment.scope(), args);
				}
			}, scriptEnvironment));
			
		}
		
		log.error("ignoring attempt to execute nonexistent function in context of {}", scriptEnvironment);
		log.error("helpful stacktrace", new Exception());
		return false;
	}
	
	/**
	 * Resumes a continuation that was previously saved from an execution in this class
	 * @param pendingKey
	 * @param scriptEnvironment
	 * @param result
	 * @return true if completed, false if continued
	 */
	boolean resumeContinuation(final String pendingKey, final ScriptEnvironment scriptEnvironment, final Object result) {
		
		assert (scriptEnvironment != null) : "cannot resume without a script execution environment";
		
		final ContinuationPending continuation = currentScriptContext.pendingContinuation(pendingKey);
		if (continuation != null) {

			return processContinuationState(execute(new ContinuationExecution() {
				
				@Override
				public void run(RhinoContext context) {
					context.resumeContinuation(continuation.getContinuation(), scriptEnvironment.scope(), result);
				}
			}, scriptEnvironment));
		}
		
		log.error("attempting to resume a non-existent continuation in {} keyed by {}", scriptEnvironment, pendingKey);
		log.error("helpful stacktrace", new Exception());
		return false;
	}
	
	private ContinuationState extractContinuationState(final ContinuationPending continuation) {
		
		final ContinuationState continuationState = (ContinuationState)continuation.getApplicationState();
		assert (continuationState != null) : "continuation captured with no state";
		
		return continuationState;
	}
	
	/**
	 * 
	 * @param continuationState
	 */
	private boolean processContinuationState(ContinuationState continuationState) {
		if (continuationState != null) {
			
			ContinuationProcessor processor = continuationProcessors.get(continuationState.type());
			
			assert processor != null : "could not find a continuation processor of type " + continuationState.type();
			
			processor.process(continuationState);
			return false;
		}
		return true;
	}
}
