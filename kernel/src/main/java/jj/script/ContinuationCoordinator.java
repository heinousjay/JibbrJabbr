package jj.script;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ContinuationPending;
import org.slf4j.Logger;

import jj.Closer;
import jj.logging.EmergencyLogger;

/**
 * Coordinates processing a continuable script, returning the 
 * information necessary to restart the script
 * @author jason
 *
 */
@Singleton
public class ContinuationCoordinator {
	
	private interface ContinuationExecution {
		void run(RhinoContext context);
	}
	
	private final Logger log;
	
	private final Provider<RhinoContext> contextProvider;
	
	private final CurrentScriptEnvironment env;
	
	private final Map<Class<? extends Continuable>, ContinuationProcessor> continuationProcessors;
	
	@Inject
	ContinuationCoordinator(
		final Provider<RhinoContext> contextProvider,
		final CurrentScriptEnvironment env,
		final @EmergencyLogger Logger log,
		final Map<Class<? extends Continuable>, ContinuationProcessor> continuationProcessors
	) {
		this.contextProvider = contextProvider;
		this.env = env;
		this.log = log;
		this.continuationProcessors = continuationProcessors;
	}
	
	private void log(final Exception e, final ScriptEnvironment scriptEnvironment) {
		log.error("unexpected problem during script execution {}", scriptEnvironment);
		log.error("", e);
	} 
	
	private ContinuationState execute(final ContinuationExecution execution, final ScriptEnvironment scriptEnvironment) {
		try (RhinoContext context = contextProvider.get()) {
			try (Closer closer = env.enterScope(scriptEnvironment)) {
				execution.run(context);
			}
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RuntimeException e) {
			log(e, scriptEnvironment);
		}
		return null;
	}
	
	/**
	 * initial execution of a script environment
	 * @param scriptEnvironment
	 * @return true if completed, false if continued
	 */
	public boolean execute(final ScriptEnvironment scriptEnvironment) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		return processContinuationState(execute(new ContinuationExecution() {
			
			@Override
			public void run(RhinoContext context) {
				context.executeScriptWithContinuations(scriptEnvironment.script(), scriptEnvironment.scope());
			}
		}, scriptEnvironment));
	}
	
	/**
	 * function execution within the context of script environment
	 * @param scriptEnvironment
	 * @param functionName
	 * @param args
	 * @return true if completed, false if continued
	 */
	public boolean execute(final ScriptEnvironment scriptEnvironment, final Callable function, final Object...args) {
		
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
	public boolean resumeContinuation(final ScriptEnvironment scriptEnvironment, final String pendingKey, final Object result) {
		
		assert (scriptEnvironment != null) : "cannot resume without a script execution environment";
		
		final ContinuationPending continuation = scriptEnvironment.continuationPending(pendingKey);
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
