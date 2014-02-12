package jj.http.server.servable.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Closer;
import jj.resource.MimeTypes;
import jj.resource.document.CurrentDocumentRequestProcessor;
import jj.resource.document.DocumentScriptEnvironment;
import jj.script.ContinuationCoordinator;
import jj.script.DependsOnScriptEnvironmentInitialization;
import jj.execution.IOTask;
import jj.execution.JJExecutor;
import jj.execution.ScriptTask;
import jj.execution.ScriptThread;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.RequestProcessor;
import jj.jjmessage.JJMessage;
import io.netty.handler.codec.http.HttpHeaders;

import org.jsoup.nodes.Document;
import org.mozilla.javascript.Callable;

/**
 * Coordinates the resources necessary to execute a request
 * to serve an HTML5 Document, and hands the result back to
 * to client
 * @author jason
 *
 */
@Singleton
public class DocumentRequestProcessor implements RequestProcessor {
	
	@SuppressWarnings("serial")
	private static class FilterList extends ArrayList<DocumentFilter> {}
	
	private final JJExecutor executor;
	
	private final DependsOnScriptEnvironmentInitialization initializer;
	
	private final ContinuationCoordinator continuationCoordinator;
	
	private final CurrentDocumentRequestProcessor currentDocument;
	
	private final DocumentScriptEnvironment documentScriptEnvironment;
	
	private final Document document;
	
	private final HttpRequest httpRequest;
	
	private final HttpResponse httpResponse;
	
	private final Set<DocumentFilter> filters;
	
	private ArrayList<JJMessage> messages; 

	@Inject
	DocumentRequestProcessor(
		final JJExecutor executor,
		final DependsOnScriptEnvironmentInitialization initializer,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentDocumentRequestProcessor currentDocument,
		final DocumentScriptEnvironment dse,
		final HttpRequest httpRequest,
		final HttpResponse httpResponse,
		// move this out!
		final Set<DocumentFilter> filters
	) {
		this.executor = executor;
		this.initializer = initializer;
		this.continuationCoordinator = continuationCoordinator;
		this.currentDocument = currentDocument;
		this.documentScriptEnvironment = dse;
		this.document = dse.document();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.filters = filters;
	}
	
	private FilterList makeFilterList(final Set<DocumentFilter> filters, final boolean io) {
		FilterList filterList = new FilterList();
		for (DocumentFilter filter: filters) {
			if (filter.needsIO(this) && io || 
				(!filter.needsIO(this) && !io)) {
				filterList.add(filter);
			}
		}
		return filterList;
	}
	
	public HttpRequest httpRequest() {
		return httpRequest;
	}
	
	public Document document() {
		return document;
	}
	
	public String baseName() {
		return documentScriptEnvironment.baseName();
	}
	
	@Override
	public void process() {
		executor.execute(new DocumentRequestProcessTask(documentScriptEnvironment));
	}
	
	private final class DocumentRequestProcessTask extends ScriptTask<DocumentScriptEnvironment> {
		
		/**
		 * @param name
		 * @param scriptEnvironment
		 */
		protected DocumentRequestProcessTask(DocumentScriptEnvironment scriptEnvironment) {
			super("processing document request at " + scriptEnvironment.baseName(), scriptEnvironment);
		}

		@Override
		protected void run() throws Exception {
			
			if (!scriptEnvironment.hasServerScript()) {
				respond();
			} else if (!scriptEnvironment.initialized()) {
				initializer.executeOnInitialization(scriptEnvironment, this);
			} else {
			
				if (result == null) {
					
					Callable readyFunction = scriptEnvironment.getFunction(DocumentScriptEnvironment.READY_FUNCTION_KEY);
					if (readyFunction != null) {
						try (Closer closer = currentDocument.enterScope(DocumentRequestProcessor.this)) {
							// should make a request object wrapper of some sort.  and perhaps response too?
							pendingKey = continuationCoordinator.execute(scriptEnvironment, readyFunction);
						}
					}
					
				} else {
					// continuations put all their own stuff back, so we only scope our resource to the initial execution. handy!
					pendingKey = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, result);
				}
				
				if (pendingKey == null) {
					respond();
				}
			}
		}
	}
	
	/**
	 * Pulls the document together and spits it out
	 * 
	 * this should all be somewhere else
	 */
	@ScriptThread
	void respond() {
		
		try {
			executeFilters(makeFilterList(filters, false));
		} catch (Exception e) {
			httpResponse.error(e);
		}
		
		final FilterList ioFilters = makeFilterList(filters, true);
		
		if (ioFilters.isEmpty()) {
			writeResponse();
		} else {
			executor.execute(new IOTask("Document filtering requiring I/O") {
				
				@Override
				public void run() {
					try {
						
						executeFilters(ioFilters);
						writeResponse();
						
					} catch (Exception e) {
						httpResponse.error(e);
					}
				}
			});
		}
	}
	
	private void executeFilters(final FilterList filterList) {
		for (DocumentFilter filter : filterList) {
			filter.filter(this);
		}
	}

	private void writeResponse() {
		// pretty printing is turned off because it inserts weird spaces
		// into the output if there are text nodes next to element node
		// and it gets REALLY ANNOYING
		document.outputSettings().prettyPrint(false).indentAmount(0);
		byte[] bytes = document.toString().getBytes(UTF_8);
		httpResponse
			.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length)
			// clients shouldn't cache these responses at all
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"))
			.content(bytes)
			.end();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + httpRequest.uri();
	}
	
	/**
	 * @return
	 */
	DocumentScriptEnvironment documentScriptEnvironment() {
		return documentScriptEnvironment;
	}

	/**
	 * adds a message intended to be processed a framework startup
	 * on the client.
	 * 
	 * currently read in the document but needs to be moved into the connection event
	 * 
	 * MOVE THIS to inside.  it's so sloppy in the html
	 * @param message
	 */
	public DocumentRequestProcessor addStartupJJMessage(final JJMessage message) {
		if (messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(message);
		return this;
	}

	public List<JJMessage> startupJJMessages() {
		ArrayList<JJMessage> messages = this.messages;
		this.messages = null;
		return messages == null ? Collections.<JJMessage>emptyList() : messages;
	}
	
	String uri() {
		return httpRequest.uri();
	}
}
