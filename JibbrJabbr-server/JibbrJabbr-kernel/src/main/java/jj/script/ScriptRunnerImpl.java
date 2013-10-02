package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.engine.HostEvent;
import jj.execution.JJExecutor;
import jj.execution.ScriptTask;
import jj.execution.ScriptThread;
import jj.http.server.JJWebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;

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
	
	private final ScriptExecutionEnvironmentHelper scriptExecutionEnvironmentHelper;
	
	private final ContinuationCoordinator continuationCoordinator;
	
	private final CurrentScriptContext context;
	
	private final JJExecutor executors;
	
	@Inject
	ScriptRunnerImpl(
		final ScriptExecutionEnvironmentHelper scriptExecutionEnvironmentHelper,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentScriptContext context,
		final JJExecutor executors
	) {
		
		this.scriptExecutionEnvironmentHelper = scriptExecutionEnvironmentHelper;
		this.continuationCoordinator = continuationCoordinator;
		this.context = context;
		this.executors = executors;
	}
	
	@ScriptThread
	private void httpRequestInitialExecution() {
		log.trace("performing initial execution of a document request");
		context.documentRequestProcessor().startingInitialExecution();

		if (continuationCoordinator.execute(context.documentScriptExecutionEnvironment())) {
			context.documentScriptExecutionEnvironment().initialized(true);
			log.trace("initial execution - completed, running ready function");
			executeReadyFunction();
		}
	}

	@ScriptThread
	private void resumeHttpRequestInitialExecution(String pendingKey, Object result) {
		log.trace("resuming initial execution of a script execution environment");
		if (continuationCoordinator.resumeContinuation(pendingKey, context.scriptExecutionEnvironment(), result)) {
			context.scriptExecutionEnvironment().initialized(true);
			log.trace("initial execution - completed, running ready function");
			executeReadyFunction();
		}
	}
	
	@ScriptThread
	private void executeReadyFunction() {
		DocumentScriptExecutionEnvironment scriptExecutionEnvironment = context.documentScriptExecutionEnvironment(); 
		Callable ready = scriptExecutionEnvironment.getFunction(READY_FUNCTION_KEY);
		if (ready == null) {
			log.trace("no ready function found for this document. responding.");
			context.documentRequestProcessor().respond();
		} else {
			log.trace("starting ready function execution");
			
			context.documentRequestProcessor().startingReadyFunction();
			
			if (continuationCoordinator.execute(scriptExecutionEnvironment, ready)) {
				log.trace("ready function execution - completed, serving document");
				context.documentRequestProcessor().respond();
			}
		}
	}

	@ScriptThread
	private void resumeReadyFunction(String pendingKey, Object result) {
		log.trace("resuming ready function execution of a script execution environment");
		
		if (continuationCoordinator.resumeContinuation(pendingKey, context.documentScriptExecutionEnvironment(), result)) {
			log.trace("ready function execution - completed, serving document");
			context.documentRequestProcessor().respond();
		}
	}
	
	@Override
	public void submit(final DocumentRequestProcessor documentRequestProcessor) {
		
		assert (documentRequestProcessor != null) : "asked to process a null document request";
		
		final String baseName = documentRequestProcessor.baseName();
		
		executors.execute(new ScriptTask("document request [" + documentRequestProcessor + "]", baseName) {

			@Override
			public void run() {
				
				log.trace("preparing to execute document request {}", baseName);
				
				DocumentScriptExecutionEnvironment scriptExecutionEnvironment = scriptExecutionEnvironmentHelper.scriptExecutionEnvironmentFor(baseName);
				if (scriptExecutionEnvironment == null) {
					try {
						context.initialize(documentRequestProcessor);
						log.trace("no script to execute for this request, responding");
						documentRequestProcessor.respond();
					} finally {
						context.end();
					}
				} else {
					
					documentRequestProcessor.associatedScriptExecutionEnvironment(scriptExecutionEnvironment);
					
					try {
						context.initialize(documentRequestProcessor);
						
						if (scriptExecutionEnvironment.initialized()) {
							executeReadyFunction();
						} else if (scriptExecutionEnvironment.initializing()) {
							// just run us again
							executors.execute(this);
						} else {
							httpRequestInitialExecution();
						}
						
					} finally {
						context.end();
					}
				}
			}
		});
	}
	
	@ScriptThread
	private void moduleInitialExecution(final RequiredModule requiredModule) {
		log.debug("performing initial execution of a required module");
		
		if (continuationCoordinator.execute(context.moduleScriptExecutionEnvironment())) {
			context.moduleScriptExecutionEnvironment().initialized(true);
			log.debug("initial execution - completed, resuming");
			completeModuleInitialization();
		}
	}

	@ScriptThread
	private void resumeModuleInitialExecution(final String pendingKey, final Object result) {
		log.debug("resuming initial execution of a required module");
		
		if (continuationCoordinator.resumeContinuation(pendingKey, context.scriptExecutionEnvironment(), result)) {
			context.scriptExecutionEnvironment().initialized(true);
			log.debug("initial execution - completed, resuming");
			completeModuleInitialization();
		}
	}
	
	private void completeModuleInitialization() {
		final RequiredModule requiredModule = context.requiredModule();
		final ModuleScriptExecutionEnvironment scriptExecutionEnvironment = context.moduleScriptExecutionEnvironment();
		
		executors.execute(new ScriptTask("module parent resumption", scriptExecutionEnvironment.baseName()) {

			@Override
			public void run() {
				log.debug("resuming module parent with exports");
				context.restore(requiredModule.parentContext());
				try {
					restartAfterContinuation(requiredModule.pendingKey(), scriptExecutionEnvironment.exports());
				} finally {
					context.end();
				}
			}
		});
	}
	
	@Override
	public void submit(final RequiredModule requiredModule) {
		final String baseName = requiredModule.baseName();
		final String identifier = requiredModule.identifier();
		
		executors.execute(new ScriptTask("module script initialization for [" + identifier + "]", baseName) {
			
			@Override
			public void run() {
				ModuleScriptExecutionEnvironment scriptExecutionEnvironment = 
					scriptExecutionEnvironmentHelper.scriptExecutionEnvironmentFor(baseName, identifier);
				assert !scriptExecutionEnvironment.initialized(): "attempting to reinitialize a required module";
				context.initialize(requiredModule, scriptExecutionEnvironment);
				try {
					moduleInitialExecution(requiredModule);
				} finally {
					context.end();
				}
			}
		});
	}

	@Override
	public void submitPendingResult(
		final JJWebSocketConnection connection,
		final String pendingKey,
		final Object result
	) {
		executors.execute(new ScriptTask("resuming continuation on [" + connection + "]", connection.baseName()) {
			
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
	private void resumeContinuation(final String pendingKey, final Object result) {
		continuationCoordinator.resumeContinuation(
			pendingKey,
			context.scriptExecutionEnvironment(),
			result
		);
	}
	
	@Override
	public void submit(final JJWebSocketConnection connection, final HostEvent hostEvent, final Object...args) {
		submit(connection, hostEvent.toString(), args);
	}
	
	@Override
	public void submit(final JJWebSocketConnection connection, final String event, final Object...args) {
		executors.execute(new ScriptTask("host event on WebSocket connection", connection.baseName()) {

			@Override
			public void run() {
				log.trace("executing event {} for connection {}", event, connection);
				context.initialize(connection);
				DocumentScriptExecutionEnvironment executionEnvironment = connection.associatedScriptExecutionEnvironment();
				Callable function = connection.getFunction(event);
				if (function == null) function = executionEnvironment.getFunction(event);
				try {
					continuationCoordinator.execute(executionEnvironment, function, args);
				} finally {
					context.end();
				}
			}
		});	
	}
	
	/** 
	 * you must be in a script thread before calling this method.
	 */
	@ScriptThread
	private void restartAfterContinuation(String pendingKey, Object result) {
		
		
		log.trace("restarting a continuation at {} with {}", pendingKey, result);
		
		switch (context.type()) {
		
		case DocumentRequest:
			switch (context.documentRequestProcessor().state()) {
			case InitialExecution:
				resumeHttpRequestInitialExecution(pendingKey, result);
				break;
			case ReadyFunctionExecution:
				resumeReadyFunction(pendingKey, result);
				break;
			default:
				resumeContinuation(pendingKey, result);
				break;
			}
			break;
			
		case ModuleInitialization:
			resumeModuleInitialExecution(pendingKey, result);
			break;
			
		case WebSocket:
		case InternalExecution:
			resumeContinuation(pendingKey, result);
			break;
		}
	}

	@Override
	public void submit(final String description, final ScriptContext saved, final String pendingKey, final Object result) {
		
		context.restore(saved);
		try {
			assert context.scriptExecutionEnvironment() != null : "attempting to restart a continuation without a script context in place";
			executors.execute(new ScriptTask(description, context.baseName()) {
	
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
