package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.execution.JJExecutor;
import jj.execution.ScriptTask;
import jj.execution.ScriptThread;
import jj.http.server.JJWebSocketConnection;
import jj.http.server.WebSocketConnectionHost;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.resource.script.ModuleScriptEnvironment;

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
	
	@ScriptThread
	private void httpRequestInitialExecution() {
		log.trace("performing initial execution of a document request");
		context.documentRequestProcessor().startingInitialExecution();

		if (continuationCoordinator.execute(context.webSocketConnectionHost())) {
			context.webSocketConnectionHost().initialized(true);
			log.trace("initial execution - completed, running ready function");
			executeReadyFunction();
		}
	}

	@ScriptThread
	private void resumeHttpRequestInitialExecution(String pendingKey, Object result) {
		log.trace("resuming initial execution of a script execution environment");
		if (continuationCoordinator.resumeContinuation(context.scriptEnvironment(), pendingKey, result)) {
			context.scriptEnvironment().initialized(true);
			log.trace("initial execution - completed, running ready function");
			executeReadyFunction();
		}
	}
	
	@ScriptThread
	private void executeReadyFunction() {
		WebSocketConnectionHost webSocketConnectionHost = context.webSocketConnectionHost(); 
		Callable ready = webSocketConnectionHost.getFunction(READY_FUNCTION_KEY);
		if (ready == null) {
			log.trace("no ready function found for this document. responding.");
			context.documentRequestProcessor().respond();
		} else {
			log.trace("starting ready function execution");
			
			context.documentRequestProcessor().startingReadyFunction();
			
			if (continuationCoordinator.execute(webSocketConnectionHost, ready)) {
				log.trace("ready function execution - completed, serving document");
				context.documentRequestProcessor().respond();
			}
		}
	}

	@ScriptThread
	private void resumeReadyFunction(String pendingKey, Object result) {
		log.trace("resuming ready function execution of a script execution environment");
		
		if (continuationCoordinator.resumeContinuation(context.webSocketConnectionHost(), pendingKey, result)) {
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
				
				log.trace("executing document request {}", baseName);
				
				try {
					context.initialize(documentRequestProcessor);
					
					if (documentRequestProcessor.documentScriptEnvironment().initialized()) {
						executeReadyFunction();
					} else if (documentRequestProcessor.documentScriptEnvironment().initializing()) {
						// just run us again
						executors.execute(this);
					} else {
						httpRequestInitialExecution();
					}
					
				} finally {
					context.end();
				}
			}
		});
	}
	
	@ScriptThread
	private void moduleInitialExecution() {
		log.debug("performing initial execution of a required module");
		
		if (continuationCoordinator.execute(context.moduleScriptEnvironment())) {
			context.moduleScriptEnvironment().initialized(true);
			log.debug("initial execution - completed, resuming");
			completeModuleInitialization();
		}
	}

	@ScriptThread
	private void resumeModuleInitialExecution(final String pendingKey, final Object result) {
		log.debug("resuming initial execution of a required module");
		
		if (continuationCoordinator.resumeContinuation(context.scriptEnvironment(), pendingKey, result)) {
			context.scriptEnvironment().initialized(true);
			log.debug("initial execution - completed, resuming");
			completeModuleInitialization();
		}
	}
	
	private void completeModuleInitialization() {
		final RequiredModule requiredModule = context.requiredModule();
		final ModuleScriptEnvironment moduleScriptEnvironment = context.moduleScriptEnvironment();
		
		executors.execute(new ScriptTask("module parent resumption", moduleScriptEnvironment.baseName()) {

			@Override
			public void run() {
				log.debug("resuming module parent with exports");
				context.restore(requiredModule.parentContext());
				try {
					restartAfterContinuation(requiredModule.pendingKey(), moduleScriptEnvironment.exports());
				} finally {
					context.end();
				}
			}
		});
	}
	
	@Override
	public void submit(final RequiredModule requiredModule, final ModuleScriptEnvironment scriptExecutionEnvironment) {
		final String baseName = requiredModule.baseName();
		final String identifier = requiredModule.identifier();
		
		executors.execute(new ScriptTask("module script initialization for [" + identifier + "]", baseName) {
			
			@Override
			public void run() {
				context.initialize(requiredModule, scriptExecutionEnvironment);
				try {
					if (!scriptExecutionEnvironment.initialized()) {
						moduleInitialExecution();
					} else {
						completeModuleInitialization();
					}
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
			context.scriptEnvironment(),
			pendingKey,
			result
		);
	}
	
	@Override
	public void submit(final JJWebSocketConnection connection, final String event, final Object...args) {
		executors.execute(new ScriptTask("host event on WebSocket connection", connection.baseName()) {

			@Override
			public void run() {
				log.trace("executing event {} for connection {}", event, connection);
				context.initialize(connection);
				WebSocketConnectionHost webSocketConnectionHost = connection.webSocketConnectionHost();
				Callable function = connection.getFunction(event);
				if (function == null) function = webSocketConnectionHost.getFunction(event);
				try {
					continuationCoordinator.execute(webSocketConnectionHost, function, args);
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
			assert context.scriptEnvironment() != null : "attempting to restart a continuation without a script context in place";
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