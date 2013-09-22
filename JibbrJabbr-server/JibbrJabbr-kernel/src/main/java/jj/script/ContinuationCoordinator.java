package jj.script;

import java.util.Map;

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
	
	private void log(final Exception e, final ScriptExecutionEnvironment scriptExecutionEnvironment) {
		log.error("unexpected problem during script execution {}", scriptExecutionEnvironment);
		log.error("", e);
	} 
	
	private ContinuationState execute(final ContinuationExecution execution, final ScriptExecutionEnvironment scriptExecutionEnvironment) {
		try (RhinoContext context = contextMaker.context()){
			execution.run(context);
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RuntimeException e) {
			log(e, scriptExecutionEnvironment);
		}
		return null;
	}
	
	/**
	 * initial execution of a script, either associated to a document, or a required module
	 * @param scriptExecutionEnvironment
	 * @return true if completed, false if continued
	 */
	boolean execute(final ScriptExecutionEnvironment scriptExecutionEnvironment) {
		
		assert (scriptExecutionEnvironment != null) : "cannot execute without a script execution environment";
		
		ScriptableObject.putConstProperty(scriptExecutionEnvironment.scope(), "scriptKey", scriptExecutionEnvironment.sha1());
		
		return processContinuationState(execute(new ContinuationExecution() {
			
			@Override
			public void run(RhinoContext context) {
				context.executeScriptWithContinuations(scriptExecutionEnvironment.script(), scriptExecutionEnvironment.scope());
			}
		}, scriptExecutionEnvironment));
	}
	
	/**
	 * function execution within the context of script associated to a document
	 * @param scriptExecutionEnvironment
	 * @param functionName
	 * @param args
	 * @return true if completed, false if continued
	 */
	boolean execute(final DocumentScriptExecutionEnvironment scriptExecutionEnvironment, final Callable function, final Object...args) {
		
		assert (scriptExecutionEnvironment != null) : "cannot execute without a script execution environment";
		
		if (function != null) {

			return processContinuationState(execute(new ContinuationExecution() {
				
				@Override
				public void run(RhinoContext context) {
					context.callFunctionWithContinuations(function, scriptExecutionEnvironment.scope(), args);
				}
			}, scriptExecutionEnvironment));
			
		}
		
		log.error("ignoring attempt to execute nonexistent function in context of {}", scriptExecutionEnvironment);
		log.error("helpful stacktrace", new Exception());
		return false;
	}
	
	/**
	 * Resumes a continuation that was previously saved from an execution in this class
	 * @param pendingKey
	 * @param scriptExecutionEnvironment
	 * @param result
	 * @return true if completed, false if continued
	 */
	boolean resumeContinuation(final String pendingKey, final ScriptExecutionEnvironment scriptExecutionEnvironment, final Object result) {
		
		assert (scriptExecutionEnvironment != null) : "cannot resume without a script execution environment";
		
		final ContinuationPending continuation = currentScriptContext.pendingContinuation(pendingKey);
		if (continuation != null) {

			return processContinuationState(execute(new ContinuationExecution() {
				
				@Override
				public void run(RhinoContext context) {
					context.resumeContinuation(continuation.getContinuation(), scriptExecutionEnvironment.scope(), result);
				}
			}, scriptExecutionEnvironment));
		}
		
		log.error("attempting to resume a non-existent continuation in {} keyed by {}", scriptExecutionEnvironment, pendingKey);
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
