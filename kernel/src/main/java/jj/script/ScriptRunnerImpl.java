package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.Closer;
import jj.execution.JJExecutor;
import jj.execution.ScriptTask;
import jj.execution.ScriptThread;
import jj.http.server.WebSocketConnection;
import jj.http.server.WebSocketConnectionHost;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.resource.document.CurrentDocumentRequestProcessor;
import jj.resource.document.DocumentScriptEnvironment;
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
	
	private final CurrentDocumentRequestProcessor document;
	
	private final JJExecutor executors;
	
	@Inject
	ScriptRunnerImpl(
		final ContinuationCoordinator continuationCoordinator,
		final CurrentScriptContext context,
		final CurrentDocumentRequestProcessor document,
		final JJExecutor executors
	) {
		this.continuationCoordinator = continuationCoordinator;
		this.context = context;
		this.document = document;
		this.executors = executors;
	}
	
	@ScriptThread
	private void httpRequestInitialExecution() {
		log.trace("performing initial execution of a document request");
		context.documentRequestProcessor().startingInitialExecution();

		if (continuationCoordinator.execute(context.webSocketConnectionHost()) == null) {
			context.webSocketConnectionHost().initialized(true);
			log.trace("initial execution - completed, running ready function");
			executeReadyFunction();
		}
	}

	@ScriptThread
	private void resumeHttpRequestInitialExecution(ContinuationPendingKey pendingKey, Object result) {
		log.trace("resuming initial execution of a script execution environment");
		if (continuationCoordinator.resumeContinuation(context.scriptEnvironment(), pendingKey, result) == null) {
			context.scriptEnvironment().initialized(true);
			log.trace("initial execution - completed, running ready function");
			submit(context.documentRequestProcessor());
			//executeReadyFunction();
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
			
			if (continuationCoordinator.execute(webSocketConnectionHost, ready) == null) {
				log.trace("ready function execution - completed, serving document");
				context.documentRequestProcessor().respond();
			}
		}
	}

	@ScriptThread
	private void resumeReadyFunction(ContinuationPendingKey pendingKey, Object result) {
		log.trace("resuming ready function execution of a script execution environment");
		
		if (continuationCoordinator.resumeContinuation(context.webSocketConnectionHost(), pendingKey, result) == null) {
			log.trace("ready function execution - completed, serving document");
			context.documentRequestProcessor().respond();
		}
	}
	
	@Override
	public void submit(final DocumentRequestProcessor documentRequestProcessor) {
		
		assert (documentRequestProcessor != null) : "asked to process a null document request";
		
		final String baseName = documentRequestProcessor.baseName();
		
		String name = "document request [" + documentRequestProcessor + "]";
		
		final DocumentScriptEnvironment dse = documentRequestProcessor.documentScriptEnvironment();
		
		executors.execute(new ScriptTask<ScriptEnvironment>(name, dse) {

			@Override
			public void run() {
				
				log.trace("executing document request {}", baseName);
				
				// this task will become a standalone outside of the script package,
				// just need to validate that the document stays available even through continuations
				try (Closer closer = document.enterScope(documentRequestProcessor)) { 
					context.initialize(documentRequestProcessor);
					
					if (documentRequestProcessor.documentScriptEnvironment().initialized()) {
						executeReadyFunction();
					} else if (documentRequestProcessor.documentScriptEnvironment().initializing()) {
						// just run us again later
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
		
		if (continuationCoordinator.execute(context.moduleScriptEnvironment()) == null) {
			context.moduleScriptEnvironment().initialized(true);
			log.debug("initial execution - completed, resuming");
			completeModuleInitialization();
		}
	}

	@ScriptThread
	private void resumeModuleInitialExecution(final ContinuationPendingKey pendingKey, final Object result) {
		log.debug("resuming initial execution of a required module");
		
		if (continuationCoordinator.resumeContinuation(context.scriptEnvironment(), pendingKey, result) == null) {
			context.scriptEnvironment().initialized(true);
			log.debug("initial execution - completed, resuming");
			completeModuleInitialization();
		}
	}
	
	private void completeModuleInitialization() {
		final RequiredModule requiredModule = context.requiredModule();
		final ModuleScriptEnvironment moduleScriptEnvironment = context.moduleScriptEnvironment();
		
		String name = "resuming module [" + requiredModule.identifier() + "] in parent [" + requiredModule.baseName() + "]";
		
		executors.execute(new ScriptTask<ScriptEnvironment>(name, moduleScriptEnvironment) {

			@Override
			public void run() {
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
		final String identifier = requiredModule.identifier();
		
		String name = "initializing module [" + identifier + "] in parent [" + requiredModule.baseName() + "]";
		
		executors.execute(new ScriptTask<ScriptEnvironment>(name, scriptExecutionEnvironment) {
			
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
