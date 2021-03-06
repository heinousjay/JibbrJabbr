package jj.document.servable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.ResourceTask;
import jj.script.DependsOnScriptEnvironmentInitialization;
import jj.script.ScriptTask;
import jj.script.ScriptThread;
import jj.util.Closer;
import jj.document.CurrentDocumentRequestProcessor;
import jj.document.DocumentScriptEnvironment;
import jj.execution.TaskRunner;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.jjmessage.JJMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
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
public class DocumentRequestProcessor {
	
	@SuppressWarnings("serial")
	private static class FilterList extends ArrayList<DocumentFilter> {}
	
	private final TaskRunner taskRunner;
	
	private final DependsOnScriptEnvironmentInitialization initializer;
	
	private final CurrentDocumentRequestProcessor currentDocument;
	
	private final DocumentScriptEnvironment documentScriptEnvironment;
	
	private final Document document;
	
	private final HttpServerRequest httpRequest;
	
	private final HttpServerResponse httpResponse;
	
	private final Set<DocumentFilter> filters;
	
	private ArrayList<JJMessage> messages; 

	@Inject
	DocumentRequestProcessor(
		final TaskRunner taskRunner,
		final DependsOnScriptEnvironmentInitialization initializer,
		final CurrentDocumentRequestProcessor currentDocument,
		final DocumentScriptEnvironment dse,
		final HttpServerRequest httpRequest,
		final HttpServerResponse httpResponse,
		// move this out!
		final Set<DocumentFilter> filters
	) {
		this.taskRunner = taskRunner;
		this.initializer = initializer;
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
	
	public HttpServerRequest httpRequest() {
		return httpRequest;
	}
	
	public Document document() {
		return document;
	}
	
	public String baseName() {
		return documentScriptEnvironment.name();
	}
	
	public void process() {
		taskRunner.execute(new DocumentRequestProcessTask(documentScriptEnvironment));
	}
	
	private final class DocumentRequestProcessTask extends ScriptTask<DocumentScriptEnvironment> {
		
		private boolean run = false;
		
		protected DocumentRequestProcessTask(DocumentScriptEnvironment scriptEnvironment) {
			super("processing document request at " + scriptEnvironment.name(), scriptEnvironment);
		}

		@Override
		protected void begin() throws Exception {
			if (!scriptEnvironment.hasServerScript()) {
				respond();
			} else if (scriptEnvironment.initializationDidError()) {
				httpResponse.error(scriptEnvironment.initializationError());
			} else if (!scriptEnvironment.initialized()) {
				initializer.executeOnInitialization(scriptEnvironment, this);
			} else {
				run = true;
				Callable readyFunction = scriptEnvironment.getFunction(DocumentScriptEnvironment.READY_FUNCTION_KEY);
				if (readyFunction != null) {
					try (Closer closer = currentDocument.enterScope(DocumentRequestProcessor.this)) {
						// should make a request object wrapper of some sort.  and perhaps response too?
						pendingKey = scriptEnvironment.execute(readyFunction);
					}
				}
			}
		}
		
		@Override
		protected void complete() throws Exception {
			if (run && pendingKey == null) {
				respond();
			}
		}
		
		@Override
		protected boolean errored(Throwable cause) {
			httpResponse.error(cause);
			return true;
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
			taskRunner.execute(new ResourceTask("Document filtering requiring I/O") {
				
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
		byte[] bytes = document.toString().getBytes(documentScriptEnvironment.charset());
		httpResponse
			.header(HttpHeaderNames.CONTENT_LENGTH, bytes.length)
			// clients shouldn't cache these responses at all
			.header(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE)
			.header(HttpHeaderNames.CONTENT_TYPE, documentScriptEnvironment.contentType())
			.content(bytes)
			.end();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + documentScriptEnvironment;
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
		return httpRequest.uriMatch().uri;
	}
}
