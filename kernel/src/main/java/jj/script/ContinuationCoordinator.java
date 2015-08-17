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
class ContinuationCoordinator implements ContinuationResumer {
	
	private interface ContinuationExecution {
		void run(RhinoContext context);
	}
	
	private final Publisher publisher;
	
	private final Provider<RhinoContext> contextProvider;
	
	private final CurrentScriptEnvironment env;
	
	private final Map<Class<? extends Continuation>, ContinuationProcessor> continuationProcessors;
	
	private final ContinuationPendingCache cache;
	
	private final IsThread is;
	
	@Inject
	ContinuationCoordinator(
		final Provider<RhinoContext> contextProvider,
		final CurrentScriptEnvironment env,
		final Publisher publisher,
		final Map<Class<? extends Continuation>, ContinuationProcessor> continuationProcessors,
		final ContinuationPendingCache cache,
		final IsThread is
	) {
		this.contextProvider = contextProvider;
		this.env = env;
		this.publisher = publisher;
		this.continuationProcessors = continuationProcessors;
		this.cache = cache;
		this.is = is;
	}
	
	private void log(final Throwable t, final ScriptEnvironment<?> scriptEnvironment) {
		publisher.publish(new ScriptExecutionError(scriptEnvironment, t));
	} 
	
	private PendingKey execute(final ScriptEnvironment<?> scriptEnvironment, final ContinuationExecution execution) {
		try (RhinoContext context = contextProvider.get()) {
			execution.run(context);
		} catch (ContinuationPending continuation) {
			return processContinuationState(extractContinuationState(continuation));
		} catch (RhinoException re) {
			// this is handled inside the RhinoContext
			throw re;
		} catch (RuntimeException e) {
			log(e, scriptEnvironment);
			throw e;
		}
		return null;
	}
	
	/**
	 * continuable script evaluation within the context of {@link ScriptEnvironment}
	 * @param scriptEnvironment The containing <code>ScriptEnvironment</code>
	 * @param script The script to evaluate.
	 * @return A key representing a pending continuation, or null if the execution completed
	 */
	PendingKey execute(final ScriptEnvironment<?> scriptEnvironment, final Script script) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		assert is.forScriptEnvironment(scriptEnvironment) : "only execute this in the right script environment!";
		
		return execute(scriptEnvironment, context -> {
			try (Closer closer = env.enterScope(scriptEnvironment)) {
				context.executeScriptWithContinuations(script, scriptEnvironment.scope());
			}
		});
	}
	
	/**
	 * continuable <code>Callable</code> execution within the context of {@link ScriptEnvironment}
	 * @param scriptEnvironment The containing <code>ScriptEnvironment</code>
	 * @param callable The <code>Callable</code> to execute
	 * @param args The arguments to the function, if any
	 * @return A key representing a pending continuation, or null if the execution completed
	 */
	PendingKey execute(final ScriptEnvironment<?> scriptEnvironment, final Callable callable, final Object...args) {
		
		assert (scriptEnvironment != null) : "cannot execute without a script execution environment";
		
		assert is.forScriptEnvironment(scriptEnvironment) : "only execute this in the right script environment!";
		
		if (callable != null) {

			return execute(scriptEnvironment, context -> {
				try (Closer closer = env.enterScope(scriptEnvironment)) {
					context.callFunctionWithContinuations(callable, scriptEnvironment.scope(), args);
				}
			});
			
		}
		publisher.publish(new CannotFindFunction(scriptEnvironment));
		return null;
	}
	
	/**
	 * Resumes a continuation that was previously saved from an execution in this class
	 * @param scriptEnvironment The containing <code>ScriptEnvironment</code>
	 * @param pendingKey The {@link PendingKey} to resume
	 * @param result The result of the resumption
	 * @return A key representing a pending continuation, or null if the execution completed
	 */
	PendingKey resumeContinuation(ScriptEnvironment<?> scriptEnvironment, final PendingKey pendingKey, final Object result) {
		
		assert (scriptEnvironment != null) : "cannot resume without a script execution environment";
		
		assert scriptEnvironment instanceof AbstractScriptEnvironment : "all script environments must be abstract script environments";
		
		assert is.forScriptEnvironment(scriptEnvironment) : "only execute this in the right script environment!";
		
		assert pendingKey != null : "cannot resume without a pending key";
		
		final AbstractScriptEnvironment<?> environment = (AbstractScriptEnvironment<?>)scriptEnvironment;
		
		final ContinuationPending continuation = ((AbstractScriptEnvironment<?>)scriptEnvironment).continuationPending(pendingKey);
		if (continuation != null) {

			return execute(scriptEnvironment, context -> {
				try (Closer closer = env.enterScope(environment, pendingKey)) {
					context.resumeContinuation(continuation.getContinuation(), environment.scope(), result);
				}
			});
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
	public void resume(final PendingKey pendingKey, final Object result) {
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
	private PendingKey processContinuationState(ContinuationState continuationState) {
		if (continuationState != null) {
			
			ContinuationProcessor processor = continuationProcessors.get(continuationState.type());
			
			assert processor != null : "could not find a continuation processor of type " + continuationState.type();
			
			processor.process(continuationState);
			
			return continuationState.continuationAs(Continuation.class).pendingKey();
		}
		return null;
	}
}
