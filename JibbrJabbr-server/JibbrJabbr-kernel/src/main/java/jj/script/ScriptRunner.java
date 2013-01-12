package jj.script;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJExecutors;
import jj.JJRunnable;
import jj.ScriptThread;
import jj.document.DocumentRequestProcessor;
import jj.hostapi.HostEvent;
import jj.jqmessage.JQueryMessage;
import jj.webbit.JJWebSocketConnection;

/**
 * DO NOT DEPEND ON THIS CLASS, DEPEND ON {@link JJExecutors}
 * 
 * Coordinates script processing in response to http requests,
 * websocket messages, and internal events.  delegates all
 * aspects of script lookup and execution externally, this class
 * is only intended to managed the execution process so that it
 * occurs in the correct thread and in the correct order
 * @author jason
 *
 */
public class ScriptRunner {

	public static final String READY_FUNCTION_KEY = "Document.ready";
	
	private final Logger log = LoggerFactory.getLogger(ScriptRunner.class);
	
	private final ScriptBundleHelper scriptBundleHelper;
	
	private final ContinuationCoordinator continuationCoordinator;
	
	private final CurrentScriptContext context;
	
	private final ScriptExecutorFactory scriptExecutorFactory;
	
	private final Map<ContinuationType, ContinuationProcessor> continuationProcessors;
	
	ScriptRunner(
		final ScriptBundleHelper scriptBundleHelper,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentScriptContext context,
		final ScriptExecutorFactory scriptExecutorFactory,
		final ContinuationProcessor[] continuationProcessors
	) {
		
		// this class has a lot of dependencies, but it does
		// a very complicated job
		
		this.scriptBundleHelper = scriptBundleHelper;
		this.continuationCoordinator = continuationCoordinator;
		this.context = context;
		this.scriptExecutorFactory = scriptExecutorFactory;
		this.continuationProcessors = makeContinuationProcessors(continuationProcessors);
	}
	
	private Map<ContinuationType, ContinuationProcessor> makeContinuationProcessors(final ContinuationProcessor[] continuationProcessors) {
		Map<ContinuationType, ContinuationProcessor> result = new HashMap<>();
		for (ContinuationProcessor processor : continuationProcessors) {
			result.put(processor.type(), processor);
		}
		return Collections.unmodifiableMap(result);
	}
	
	@ScriptThread
	private void httpRequestInitialExecution() {
		log.trace("performing initial execution of a document request");
		
		context.httpRequest().startingInitialExecution();
		final ContinuationState continuationState = 
				continuationCoordinator.execute(context.associatedScriptBundle());
		
		if (continuationState == null) {
			context.associatedScriptBundle().initialized(true);
			log.trace("initial execution - completed, running ready function");
			executeReadyFunction();
		} else {
			log.trace("initial execution - continuation. storing execution state");
			processContinuationState(continuationState);
		}
	}

	@ScriptThread
	private void resumeHttpRequestInitialExecution(String pendingKey, Object result) {
		log.trace("resuming initial execution of a script bundle");
		
		final ContinuationState continuationState = 
				continuationCoordinator.resumeContinuation(pendingKey, context.scriptBundle(), result);
		
		if (continuationState == null) {
			context.scriptBundle().initialized(true);
			log.trace("initial execution - completed, running ready function");
			executeReadyFunction();
		} else {
			log.trace("initial execution - continuation. storing execution state");
			processContinuationState(continuationState);
		}
	}
	
	@ScriptThread
	private void executeReadyFunction() {
		AssociatedScriptBundle scriptBundle = context.associatedScriptBundle(); 
		Callable ready = scriptBundle.getFunction(READY_FUNCTION_KEY);
		if (ready == null) {
			// TODO smarter exception here!
			throw new IllegalStateException("document script is defined with no ready function.  it won't work.");
		}
		log.trace("starting ready function execution");
		
		context.httpRequest().startingReadyFunction();
		final ContinuationState continuationState = 
				continuationCoordinator.execute(scriptBundle, READY_FUNCTION_KEY);
		
		if (continuationState == null) {
			log.trace("ready function execution - completed, serving document");
			context.documentRequestProcessor().respond();
		} else {
			log.trace("ready function execution - continuation, storing execution state");
			processContinuationState(continuationState);
		}
	}

	@ScriptThread
	private void resumeReadyFunction(String pendingKey, Object result) {
		log.trace("resuming ready function execution of a script bundle");
		
		final ContinuationState continuationState = 
				continuationCoordinator.resumeContinuation(pendingKey, context.associatedScriptBundle(), result);
		
		if (continuationState == null) {
			log.trace("ready function execution - completed, serving document");
			context.documentRequestProcessor().respond();
		} else {
			log.trace("ready function execution - continuation, storing execution state");
			processContinuationState(continuationState);
		}
	}
	
	public void submit(final DocumentRequestProcessor documentRequestProcessor) {
		
		assert (documentRequestProcessor != null) : "asked to process a null document request";
		
		final String baseName = documentRequestProcessor.baseName();
		
		submit(baseName, new JJRunnable("document ready function execution") {
			
			@Override
			protected void innerRun() throws Exception {
				
				log.trace("preparing to execute document request {}", baseName);
				
				AssociatedScriptBundle scriptBundle = scriptBundleHelper.scriptBundleFor(baseName);
				if (scriptBundle == null) {
					try {
						context.initialize(documentRequestProcessor);
						log.trace("no script to execute for this request, responding");
						documentRequestProcessor.respond();
					} finally {
						context.end();
					}	
				} else {
					
					documentRequestProcessor.scriptBundle(scriptBundle);
					
					try {
						context.initialize(documentRequestProcessor);
						
						if (scriptBundle.initialized()) {
							executeReadyFunction();
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
		
		final ContinuationState continuationState = 
				continuationCoordinator.execute(context.moduleScriptBundle());
		
		if (continuationState == null) {
			context.moduleScriptBundle().initialized(true);
			log.debug("initial execution - completed, resuming");
			resumeModuleParent();
		} else {
			log.debug("initial execution - continuation. storing execution state");
			processContinuationState(continuationState);
		}
	}

	@ScriptThread
	private void resumeModuleInitialExecution(final String pendingKey, final Object result) {
		log.debug("resuming initial execution of a required module");
		
		final ContinuationState continuationState = 
				continuationCoordinator.resumeContinuation(pendingKey, context.scriptBundle(), result);
		
		if (continuationState == null) {
			context.scriptBundle().initialized(true);
			log.debug("initial execution - completed, resuming");
			resumeModuleParent();
		} else {
			log.debug("initial execution - continuation. storing execution state");
			processContinuationState(continuationState);
		}
	}
	
	private void resumeModuleParent() {
		final RequiredModule requiredModule = context.requiredModule();
		final ModuleScriptBundle scriptBundle = context.moduleScriptBundle();
		
		submit(scriptBundle.baseName(), new JJRunnable("module parent resumption") {
			
			@Override
			protected void innerRun() throws Exception {
				log.debug("resuming module parent with exports");
				context.restore(requiredModule.parentContext());
				try {
					restartAfterContinuation(requiredModule.pendingKey(), scriptBundle.exports());
				} finally {
					context.end();
				}
			}
		});
	}
	
	public void submit(final RequiredModule requiredModule) {
		final String baseName = requiredModule.baseName();
		final String identifier = requiredModule.identifier();
		
		submit(baseName, new JJRunnable("module script initialization for " + identifier) {
			
			@Override
			protected void innerRun() throws Exception {
				ModuleScriptBundle scriptBundle = scriptBundleHelper.scriptBundleFor(baseName, identifier);
				assert !scriptBundle.initialized(): "attempting to reinitialize a required module";
				context.initialize(scriptBundle, requiredModule);
				try {
					moduleInitialExecution(requiredModule);
				} finally {
					context.end();
				}
			}
		});
	}

	private void resumeContinuation(
		final JJWebSocketConnection connection,
		final String pendingKey,
		final Object result
	) {
		submit(connection.baseName(), new JJRunnable("resuming continuation on " + connection) {
			
			@Override
			protected void innerRun() throws Exception {
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
		processContinuationState(
			continuationCoordinator.resumeContinuation(
				pendingKey,
				context.scriptBundle(),
				result
			)
		);
	}
	
	public void submit(final JJWebSocketConnection connection, final HostEvent hostEvent, final Object...args) {
		submit(connection.baseName(), new JJRunnable("host event on WebSocket connection") {
			@Override
			protected void innerRun() throws Exception {
				log.trace("executing host event {} for connection {}", hostEvent, connection);
				context.initialize(connection);
				try {
					processContinuationState(
						continuationCoordinator.execute(connection.associatedScriptBundle(), hostEvent.toString(), args)
					);
				} finally {
					context.end();
				}
			}
		});	
	}
	
	private void processEvent(
		final JJWebSocketConnection connection,
		final String eventName,
		final Object...args
	) {
		submit(connection.baseName(), new JJRunnable("client event on WebSocket connection") {
			@Override
			protected void innerRun() throws Exception {
				context.initialize(connection);
				try {
					log.trace("executing script event {} for scriptBundle {}", eventName, connection.associatedScriptBundle());
					processContinuationState(continuationCoordinator.execute(connection.associatedScriptBundle(), eventName));
				} finally {
					context.end();
				}
			}
		});
	}
	
	public void submit(
		final JJWebSocketConnection connection,
		final JQueryMessage message
	) {
		log.trace("preparing to process an incoming message {} for connection {}", message, connection);
		
		switch (message.type()) {
		case Result:
			resumeContinuation(connection, message.result().id, message.result().value);
			break;
		case Event:
			processEvent(connection, EventNameHelper.makeEventName(message));
			break;
		default:
			log.warn("received a message that makes no sense {}", message);
		}
	}
	
	/**
	 * Generic method of switching off to the script executor for a given baseName
	 * @param baseName
	 * @param runnable
	 */
	private void submit(final String baseName, final Runnable runnable) {
		scriptExecutorFactory.executorFor(baseName).submit(runnable);
	}
	
	/**
	 * 
	 * @param continuationState
	 */
	private void processContinuationState(ContinuationState continuationState) {
		if (continuationState != null) {
			
			ContinuationProcessor processor = continuationProcessors.get(continuationState.type());
			
			assert processor != null : "could not find a continuation processor of type " + continuationState.type();
			
			processor.process(continuationState);
		}
	}
	
	/** 
	 * you must be in a script thread before calling this method.
	 */
	@ScriptThread
	void restartAfterContinuation(String pendingKey, Object result) {
		
		assert scriptExecutorFactory.isScriptThread() : "attempting to restart a continuation from the wrong thread";
		assert context.scriptBundle() != null : "attempting to restart a continuation without a script context in place";
		
		log.trace("restarting a continuation at {} with {}", pendingKey, result);
		
		switch (context.type()) {
		
		case HttpRequest:
			switch (context.httpRequest().state()) {
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
}
