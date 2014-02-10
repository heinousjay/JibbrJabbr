package jj.http.server.servable.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.MimeTypes;
import jj.resource.document.DocumentScriptEnvironment;
import jj.script.ScriptRunner;
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
	
	/** the executors in which we run */
	private final JJExecutor executors;
	
	private final ScriptRunner scriptRunner;
	
	private final DocumentScriptEnvironment documentScriptEnvironment;
	
	private final Document document;
	
	private final HttpRequest httpRequest;
	
	private final HttpResponse httpResponse;
	
	private final Set<DocumentFilter> filters;
	
	private ArrayList<JJMessage> messages; 
	
	private volatile DocumentRequestState state = DocumentRequestState.Uninitialized;

	@Inject
	DocumentRequestProcessor(
		final JJExecutor executors,
		final ScriptRunner scriptRunner,
		final DocumentScriptEnvironment dse,
		final HttpRequest httpRequest,
		final HttpResponse httpResponse,
		final Set<DocumentFilter> filters
	) {
		this.executors = executors;
		this.scriptRunner = scriptRunner;
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
		if (documentScriptEnvironment.hasServerScript()) {
			scriptRunner.submit(this);
		} else {
			String name = "responding to document request [" + baseName() + "]";
			executors.execute(new ScriptTask<DocumentScriptEnvironment>(name, documentScriptEnvironment) {
				
				@Override
				protected void run() throws Exception {
					respond();
				}
			});
		}
	}
	
	/**
	 * Pulls the document together and spits it out
	 */
	@ScriptThread
	public void respond() {
		
		assert executors.isScriptThreadFor(baseName()) : "must be called in a script thread for " + baseName();
		try {
			executeFilters(makeFilterList(filters, false));
		} catch (Exception e) {
			httpResponse.error(e);
		}
		
		final FilterList ioFilters = makeFilterList(filters, true);
		
		if (ioFilters.isEmpty()) {
			writeResponse();
		} else {
			executors.execute(new IOTask("Document filtering requiring I/O") {
				
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
	
	
	public DocumentRequestProcessor startingInitialExecution() {
		state = DocumentRequestState.InitialExecution;
		documentScriptEnvironment.initializing(true);
		return this;
	}
	
	
	public DocumentRequestProcessor startingReadyFunction() {
		state = DocumentRequestState.ReadyFunctionExecution;
		documentScriptEnvironment.initialized(true);
		return this;
	}
	
	public DocumentRequestState state() {
		return state;
	}
	
	/**
	 * @return
	 */
	public DocumentScriptEnvironment documentScriptEnvironment() {
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
	
	public String uri() {
		return httpRequest.uri();
	}
}
