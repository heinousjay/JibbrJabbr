package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.execution.JJExecutor;
import jj.execution.ScriptTask;
import jj.execution.ScriptThread;
import jj.http.server.WebSocketConnection;

/**
 * Coordinates script processing in response to http requests,
 * websocket messages, and internal events.  delegates all
 * aspects of script lookup and execution externally, this class
 * is only intended to managed the execution process so that it
 * occurs in the correct thread and in the correct order
 * @author jason
 *
 */
@Singleton
class ScriptRunnerImpl implements ScriptRunnerInternal {

	private final Logger log = LoggerFactory.getLogger(ScriptRunnerImpl.class);
	
	private final ContinuationCoordinator continuationCoordinator;
	
	private final CurrentScriptContext context;
	
	private final JJExecutor executors;
	
	@Inject
	ScriptRunnerImpl(
		final ContinuationCoordinator continuationCoordinator,
		final CurrentScriptContext context,
		final JJExecutor executors
	) {
		this.continuationCoordinator = continuationCoordinator;
		this.context = context;
		this.executors = executors;
	}

	@Override
	public void submitPendingResult(
		final WebSocketConnection connection,
		final ContinuationPendingKey pendingKey,
		final Object result
	) {
		executors.execute(new ScriptTask<ScriptEnvironment>("resuming continuation on [" + connection + "]", connection.webSocketConnectionHost()) {
			
			@Override
			public void run() {
				context.initialize(connection);
				try {
					resumeContinuation(pendingKey, result);
				} finally {
					context.end();
				}
			}
		});
	}
	
	@ScriptThread
	private void resumeContinuation(final ContinuationPendingKey pendingKey, final Object result) {
		continuationCoordinator.resumeContinuation(
			context.scriptEnvironment(),
			pendingKey,
			result
		);
	}
	
	/** 
	 * you must be in a script thread before calling this method.
	 */
	@ScriptThread
	private void restartAfterContinuation(ContinuationPendingKey pendingKey, Object result) {
		
		
		log.trace("restarting a continuation at {} with {}", pendingKey, result);
		
		switch (context.type()) {
		
		case DocumentRequest:
			throw new AssertionError("SHOULD NEVER HAPPEN AGAIN");
			
		case ModuleInitialization:
			throw new AssertionError("SHOULD NEVER HAPPEN AGAIN");
			
		case WebSocket:
		case InternalExecution:
			resumeContinuation(pendingKey, result);
			break;
		}
	}

	@Override
	public void submit(final String description, final ScriptContext saved, final ContinuationPendingKey pendingKey, final Object result) {
		
		context.restore(saved);
		try {
			assert context.scriptEnvironment() != null : "attempting to restart a continuation without a script context in place";
			executors.execute(new ScriptTask<ScriptEnvironment>(description, context.webSocketConnectionHost()) {
	
				@Override
				protected void run() throws Exception {
					try {
						context.restore(saved);
						restartAfterContinuation(pendingKey, result);
					} finally {
						context.end();
					}
				}
				
			});
		} finally {
			context.end();
		}
	}
}
