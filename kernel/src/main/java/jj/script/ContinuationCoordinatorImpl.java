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
class ContinuationCoordinatorImpl implements ContinuationCoordinator {
	
	private interface ContinuationExecution {
		void run(RhinoContext context);
	}
	
	private final Logger log;
	
	private final Provider<RhinoContext> contextProvider;
	
	private final CurrentScriptEnvironment env;
	
	private final Map<Class<? extends Continuable>, ContinuationProcessor> continuationProcessors;
	
	@Inject
	ContinuationCoordinatorImpl(
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
			execution.run(context);
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
	@Override
	public ContinuationPendingKey execute(final ScriptEnvironment scriptEnvironment) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		return processContinuationState(execute(new ContinuationExecution() {
			
			@Override
			public void run(RhinoContext context) {
				try (Closer closer = env.enterScope(scriptEnvironment)) {
					context.executeScriptWithContinuations(scriptEnvironment.script(), scriptEnvironment.scope());
				}
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
	@Override
	public ContinuationPendingKey execute(final ScriptEnvironment scriptEnvironment, final Callable function, final Object...args) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		if (function != null) {

			return processContinuationState(execute(new ContinuationExecution() {
				
				@Override
				public void run(RhinoContext context) {
					try (Closer closer = env.enterScope(scriptEnvironment)) {
						context.callFunctionWithContinuations(function, scriptEnvironment.scope(), args);
					}
				}
			}, scriptEnvironment));
			
		}
		
		log.error("ignoring attempt to execute nonexistent function in context of {}", scriptEnvironment);
		log.error("helpful stacktrace", new Exception());
		return null;
	}
	
	/**
	 * Resumes a continuation that was previously saved from an execution in this class
	 * @param pendingKey
	 * @param scriptEnvironment
	 * @param result
	 * @return true if completed, false if continued
	 */
	@Override
	public ContinuationPendingKey resumeContinuation(ScriptEnvironment scriptEnvironment, final ContinuationPendingKey pendingKey, final Object result) {
		
		assert (scriptEnvironment != null) : "cannot resume without a script execution environment";
		
		assert scriptEnvironment instanceof AbstractScriptEnvironment : "all script environments must be abstract script environments";
		
		final AbstractScriptEnvironment environment = (AbstractScriptEnvironment)scriptEnvironment;
		
		final ContinuationPending continuation = ((AbstractScriptEnvironment)scriptEnvironment).continuationPending(pendingKey);
		if (continuation != null) {

			return processContinuationState(execute(new ContinuationExecution() {
				
				@Override
				public void run(RhinoContext context) {
					try (Closer closer = env.enterScope(environment, pendingKey)) {
						context.resumeContinuation(continuation.getContinuation(), environment.scope(), result);
					}
				}
			}, scriptEnvironment));
		}
		
		log.error("attempting to resume a non-existent continuation in {} keyed by {}", scriptEnvironment, pendingKey);
		log.error("helpful stacktrace", new Exception());
		return null;
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
	private ContinuationPendingKey processContinuationState(ContinuationState continuationState) {
		if (continuationState != null) {
			
			ContinuationProcessor processor = continuationProcessors.get(continuationState.type());
			
			assert processor != null : "could not find a continuation processor of type " + continuationState.type();
			
			processor.process(continuationState);
			
			return continuationState.continuableAs(Continuable.class).pendingKey();
		}
		return null;
	}
}
