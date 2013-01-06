package jj.script;

import java.util.Map;

import jj.document.DocumentRequestProcessor;
import jj.jqmessage.JQueryMessage;
import jj.script.ContinuationState.Returns;
import jj.script.continuation.RestRequest;
import jj.webbit.JJHttpRequest;
import jj.webbit.JJWebSocketConnection;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains the current context for a thread
 * executing a script
 * @author jason
 *
 */
public class CurrentScriptContext {
	
	private final Logger log = LoggerFactory.getLogger(CurrentScriptContext.class);
	
	/**
	 * maintains a per-thread nestable context that the ScriptRunner et al can use
	 * to keep track of what it's working for
	 */
	private static final ThreadLocal<ScriptContext> currentContext = new ThreadLocal<>();
	
	public ScriptBundle scriptBundle() {
		return moduleScriptBundle() != null ?
			moduleScriptBundle() :
			associatedScriptBundle();
	}
	
	public ModuleScriptBundle moduleScriptBundle() {
		return currentContext.get().moduleScriptBundle;
	}
	
	public AssociatedScriptBundle associatedScriptBundle() {
		return currentContext.get().associatedScriptBundle;
	}
	
	public String baseName() {
		return scriptBundle().baseName();
	}
	
	public JJWebSocketConnection connection() {
		return currentContext.get().connection;
	}
	
	public DocumentRequestProcessor documentRequestProcessor() {
		return currentContext.get().documentRequestProcessor;
	}
	
	public JJHttpRequest httpRequest() {
		return currentContext.get().documentRequestProcessor.httpRequest();
	}
	
	public Document document() {
		return currentContext.get().documentRequestProcessor.document();
	}
	
	public void initialize(final ModuleScriptBundle moduleScriptBundle) {
		currentContext.set(new ScriptContext(currentContext.get(), moduleScriptBundle));
	}
	
	public void initialize(final AssociatedScriptBundle associatedScriptBundle) {
		currentContext.set(new ScriptContext(currentContext.get(), associatedScriptBundle));
	}
	
	public void initialize(final JJWebSocketConnection connection) {
		currentContext.set(new ScriptContext(currentContext.get(), connection));
	}
	
	public void initialize(final DocumentRequestProcessor documentRequestProcessor) {
		currentContext.set(new ScriptContext(currentContext.get(), documentRequestProcessor));
	}
	
	public ScriptContext save() {
		return currentContext.get();
	}
	
	public void restore(ScriptContext scriptContext) {
		assert currentContext.get() == null : "cannot restore a context on top of another context";
		currentContext.set(scriptContext);
	}
	
	private static final String PENDING_KEY = "pending continuation [%s]";

	private Object onBehalfOf() {
		return connection() != null ? connection() : httpRequest();
	}

	private void addPendingContinuation(
		Map<String, Object> data,
		String key,
		ContinuationPending continuationPending
	) {
		String pendingKey = String.format(PENDING_KEY, key);
		
		if (data.containsKey(pendingKey)) {
			log.error("more than one continuation pending under key {} for {}", key, onBehalfOf());
			log.error("current pending : {}", data.get(pendingKey));
			log.error("new pending : {}", continuationPending);
			throw new AssertionError("more than one continuation pending for a connection.");
		}
		
		data.put(pendingKey, continuationPending);
	}

	private ContinuationPending pendingContinuation(Map<String, Object> data, String key) {
		String pendingKey = String.format(PENDING_KEY, key);
		
		if (data.get(pendingKey) == null) {
			log.error("asked to retrieve a pending continuation under the wrong key {} for {}", key, onBehalfOf());
			throw new AssertionError("asked to retrieve a pending continuation under the wrong key");
		}
		
		return (ContinuationPending)data.remove(pendingKey);
	}

	
	private static Returns returnsString = new Returns() {
		public String transform(String value) {
			return value;
		}
		
		public String toString(){
			return "String";
		}
	};
	
	/**
	 * Prepares a continuation and throws it.  Returns the ContinuationPending
	 * so that callers can write
	 * <code>throw context.prepareContinuation(...)</code>
	 * so the compiler stays happy, but this method never returns normally
	 * 
	 * the result will return a string
	 * 
	 * @param jQueryMessage
	 * @return
	 */
	public ContinuationPending prepareContinuation(JQueryMessage jQueryMessage) {
		return prepareContinuation(jQueryMessage, returnsString);
	}
	
	/**
	 * Prepares a continuation and throws it.  Returns the ContinuationPending
	 * so that callers can write
	 * <code>throw context.prepareContinuation(...)</code>
	 * so the compiler stays happy, but this method never returns normally
	 * 
	 * @param jQueryMessage
	 * @param returns
	 * @return
	 */
	public ContinuationPending prepareContinuation(JQueryMessage jQueryMessage, Returns returns) {
		
		ContinuationState continuationState = new ContinuationState(jQueryMessage, returns);
		throw prepareContinuation(jQueryMessage.resultId(), continuationState);
	}
	
	public ContinuationPending prepareContinuation(RestRequest restRequest) {
		return prepareContinuation(restRequest, returnsString);
	}
	
	public ContinuationPending prepareContinuation(RestRequest restRequest, Returns returns) {
		ContinuationState continuationState = new ContinuationState(restRequest, returns);
		throw prepareContinuation(restRequest.id(), continuationState);
	}
	
	private ContinuationPending prepareContinuation(String pendingId, ContinuationState continuationState) {
		Context context = Context.enter();
		try {
			ContinuationPending continuation = context.captureContinuation();
			continuation.setApplicationState(continuationState);
			if (connection() != null) {
				addPendingContinuation(connection().data(), pendingId, continuation);
			} else if (httpRequest() != null) {
				addPendingContinuation(httpRequest().data(), pendingId, continuation);
			} else {
				throw new AssertionError("attempting a continuation with nothing to coordinate resumption");
			}
			return continuation;
		} finally {
			Context.exit();
		}
	}
	
	public ContinuationPending pendingContinuation(String key) {
		if (connection() != null) {
			return pendingContinuation(connection().data(), key);
		} else if (httpRequest() != null) {
			return pendingContinuation(httpRequest().data(), key);
		}
		
		throw new AssertionError("pending continuation requested when we don't have a connection");
	}
	
	public void end() {
		// if this was a connection, let it know the context ended
		if (connection() != null) {
			connection().end();
		}
		currentContext.set(currentContext.get().parent);
	}
}
