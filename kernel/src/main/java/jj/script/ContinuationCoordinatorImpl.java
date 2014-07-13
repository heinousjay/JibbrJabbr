package jj.script;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;

import jj.event.Publisher;
import jj.util.Closer;

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
	
	private final Publisher publisher;
	
	private final Provider<RhinoContext> contextProvider;
	
	private final CurrentScriptEnvironment env;
	
	private final Map<Class<? extends Continuation>, ContinuationProcessor> continuationProcessors;
	
	private final ContinuationPendingCache cache;
	
	@Inject
	ContinuationCoordinatorImpl(
		final Provider<RhinoContext> contextProvider,
		final CurrentScriptEnvironment env,
		final Publisher publisher,
		final Map<Class<? extends Continuation>, ContinuationProcessor> continuationProcessors,
		final ContinuationPendingCache cache
	) {
		this.contextProvider = contextProvider;
		this.env = env;
		this.publisher = publisher;
		this.continuationProcessors = continuationProcessors;
		this.cache = cache;
	}
	
	private void log(final Throwable t, final ScriptEnvironment scriptEnvironment) {
		publisher.publish(new ScriptExecutionError(scriptEnvironment, t));
	} 
	
	private ContinuationState execute(final ContinuationExecution execution, final ScriptEnvironment scriptEnvironment) {
		try (RhinoContext context = contextProvider.get()) {
			execution.run(context);
		} catch (ContinuationPending continuation) {
			return extractContinuationState(continuation);
		} catch (RhinoException re) {
			// this is handled inside the RhinoContext
			throw re;
		} catch (RuntimeException e) {
			log(e, scriptEnvironment);
			throw e;
		}
		return null;
	}
	
	@Override
	public ContinuationPendingKey execute(final ScriptEnvironment scriptEnvironment, final Script script) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		return processContinuationState(execute(new ContinuationExecution() {
			
			@Override
			public void run(RhinoContext context) {
				try (Closer closer = env.enterScope(scriptEnvironment)) {
					context.executeScriptWithContinuations(script, scriptEnvironment.scope());
				}
			}
		}, scriptEnvironment));
	}
	
	/**
	 * initial execution of a script environment.  only available to the Initializer task
	 * @param scriptEnvironment The script environment
	 */
	ContinuationPendingKey execute(final ScriptEnvironment scriptEnvironment) {
		
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
		publisher.publish(new CannotFindFunction(scriptEnvironment));
		return null;
	}
	
	@Override
	public void awaitContinuation(ScriptTask<? extends ScriptEnvironment> task) {
		cache.storeForContinuation(task);
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
		
		publisher.publish(new CannotFindContinuation(scriptEnvironment, pendingKey));
		
		return null;
	}
	
	/**
	 * Kinda weird that this is in here, but it's a convenience for now
	 * TODO decide if this is okay here
	 * @param pendingKey
	 * @param result
	 */
	@Override
	public void resume(final ContinuationPendingKey pendingKey, final Object result) {
		cache.resume(pendingKey, result);
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
			
			return continuationState.continuationAs(Continuation.class).pendingKey();
		}
		return null;
	}
}
