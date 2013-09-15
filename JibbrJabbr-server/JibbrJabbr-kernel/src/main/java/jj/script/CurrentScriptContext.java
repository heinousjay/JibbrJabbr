package jj.script;

import java.io.Closeable;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.DataStore;
import jj.jjmessage.JJMessage;
import jj.http.HttpRequest;
import jj.http.server.JJWebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.ContinuationPending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains the current context for a thread
 * executing a script.  basically a multimode
 * repository of information 
 * @author jason
 *
 */
@Singleton
public class CurrentScriptContext implements Closeable {
	
	private final Logger log = LoggerFactory.getLogger(CurrentScriptContext.class);
	
	/**
	 * maintains a per-thread nestable context that the ScriptRunner et al can use
	 * to keep track of what it's working for
	 */
	private final ThreadLocal<ScriptContext> currentContext = new ThreadLocal<>();
	
	private final RhinoContextMaker rhinoContextMaker;
	
	@Inject
	CurrentScriptContext(final RhinoContextMaker rhinoContextMaker) {
		this.rhinoContextMaker = rhinoContextMaker;
	}
	
	public ScriptContextType type() {
		return currentContext.get().type;
	}
	
	public ScriptExecutionEnvironment scriptExecutionEnvironment() {
		return moduleScriptExecutionEnvironment() != null ?
			moduleScriptExecutionEnvironment() :
			documentScriptExecutionEnvironment();
	}
	
	public ModuleScriptExecutionEnvironment moduleScriptExecutionEnvironment() {
		return currentContext.get().moduleScriptExecutionEnvironment;
	}
	
	public DocumentScriptExecutionEnvironment documentScriptExecutionEnvironment() {
		return currentContext.get().associatedScriptExecutionEnvironment;
	}
	
	public String baseName() {
		return scriptExecutionEnvironment().baseName();
	}
	
	RequiredModule requiredModule() {
		return currentContext.get().requiredModule;
	}
	
	public JJWebSocketConnection connection() {
		return currentContext.get().connection;
	}
	
	public DocumentRequestProcessor documentRequestProcessor() {
		return currentContext.get().documentRequestProcessor;
	}
	
	public HttpRequest httpRequest() {
		return currentContext.get().documentRequestProcessor.httpRequest();
	}
	
	public Document document() {
		return currentContext.get().documentRequestProcessor.document();
	}
	
	public ScriptContext initialize(final RequiredModule requiredModule, final ModuleScriptExecutionEnvironment moduleScriptExecutionEnvironment) {
		currentContext.set(new ScriptContext(currentContext.get(), requiredModule, moduleScriptExecutionEnvironment));
		return currentContext.get();
	}
	
	public ScriptContext initialize(final DocumentScriptExecutionEnvironment associatedScriptExecutionEnvironment) {
		currentContext.set(new ScriptContext(currentContext.get(), associatedScriptExecutionEnvironment));
		return currentContext.get();
	}
	
	public ScriptContext initialize(final JJWebSocketConnection connection) {
		currentContext.set(new ScriptContext(currentContext.get(), connection));
		return currentContext.get();
	}
	
	public ScriptContext initialize(final DocumentRequestProcessor documentRequestProcessor) {
		currentContext.set(new ScriptContext(currentContext.get(), documentRequestProcessor));
		return currentContext.get();
	}
	
	ScriptContext save() {
		return currentContext.get();
	}
	
	public void restore(ScriptContext scriptContext) {
		assert currentContext.get() == null : "cannot restore a context on top of another context";
		currentContext.set(scriptContext);
	}
	
	private static final String PENDING_KEY = "pending continuation [%s]";

	private Object onBehalfOf() {
		switch (type()) {
		case DocumentRequest:
			return documentRequestProcessor();
		
		case InternalExecution:
			return documentScriptExecutionEnvironment();
			
		case ModuleInitialization:
			return moduleScriptExecutionEnvironment();
			
		case WebSocket:
			return connection();
		}
		
		throw new AssertionError();
	}

	private void addPendingContinuation(
		DataStore dataStore,
		String key,
		ContinuationPending continuationPending
	) {
		String pendingKey = String.format(PENDING_KEY, key);
		
		if (dataStore.containsData(pendingKey)) {
			log.error("more than one continuation pending under key {} for {}", key, onBehalfOf());
			log.error("current pending : {}", dataStore.data(pendingKey));
			log.error("new pending : {}", continuationPending);
			throw new AssertionError("more than one continuation pending for a connection.");
		}
		log.trace("storing a continuation under key {} for  {}", key, onBehalfOf());
		dataStore.data(pendingKey, continuationPending);
	}

	private ContinuationPending pendingContinuation(DataStore dataStore, String key) {
		String pendingKey = String.format(PENDING_KEY, key);
		
		if (!dataStore.containsData(pendingKey)) {
			log.error("asked to retrieve a pending continuation under the wrong key {} for {}", key, onBehalfOf());
			throw new AssertionError("asked to retrieve a pending continuation under the wrong key");
		}
		
		return (ContinuationPending)dataStore.removeData(pendingKey);
	}
	
	/**
	 * Prepares a continuation and throws it.  Returns the ContinuationPending
	 * so that callers can write
	 * <code>throw context.prepareContinuation(...)</code>
	 * so the compiler stays happy, but this method never returns normally
	 * 
	 * the result will return a string
	 * 
	 * @param jjMessage
	 * @return
	 */
	public ContinuationPending prepareContinuation(JJMessage jjMessage) {
		ContinuationState continuationState = new ContinuationState(jjMessage);
		throw prepareContinuation(jjMessage.id(), continuationState);
	}
	
	/**
	 * Prepares and throws a continuation for a rest request
	 * @param restRequest
	 * @return
	 */
	public ContinuationPending prepareContinuation(RestRequest restRequest) {
		ContinuationState continuationState = new ContinuationState(restRequest);
		throw prepareContinuation(restRequest.id(), continuationState);
	}
	
	/**
	 * prepares and throws a continuation to require a new module
	 * @param require
	 * @return
	 */
	public ContinuationPending prepareContinuation(RequiredModule require) {
		ContinuationState continuationState = new ContinuationState(require);
		throw prepareContinuation(require.pendingKey(), continuationState);
	}
	
	/**
	 * captures a continuation from the rhino interpreter, and stores the information
	 * necessary for resumption
	 * @param pendingId 
	 * @param continuationState
	 * @return
	 */
	private ContinuationPending prepareContinuation(String pendingId, ContinuationState continuationState) {
		
		try(RhinoContext context = rhinoContextMaker.context()) {
			ContinuationPending continuation = context.captureContinuation();
			continuation.setApplicationState(continuationState);
			
			switch(type()) {
			case DocumentRequest:
				addPendingContinuation(documentRequestProcessor(), pendingId, continuation);
				break;
			
			case WebSocket:
				addPendingContinuation(connection(), pendingId, continuation);
				break;
			
			case ModuleInitialization:
				addPendingContinuation(requiredModule(), pendingId, continuation);
				break;
				
			default:
				throw new AssertionError("attempting a continuation with nothing to coordinate resumption");
			}
			
			return continuation;
		} catch (Exception e) {
			throw new AssertionError("could not capture a continuation", e);
		}
	}
	
	public ContinuationPending pendingContinuation(String key) {
		
		switch(type()) {
		case DocumentRequest:
			return pendingContinuation(documentRequestProcessor(), key);
		
		case WebSocket:
			return pendingContinuation(connection(), key);
		
		case ModuleInitialization:
			return pendingContinuation(requiredModule(), key);
			
		default:
			throw new AssertionError("looking for a pending continuation in a context without ");
		}
	}
	
	public void end() {
		// if this was a connection, let it know the context ended
		if (connection() != null) {
			connection().end();
		}
		currentContext.set(currentContext.get().parent);
	}

	@Override
	public void close() {
		end();
	}
}
