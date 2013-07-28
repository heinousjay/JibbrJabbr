package jj.http.server.servable.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jj.resource.HtmlResource;
import jj.script.AssociatedScriptBundle;
import jj.execution.JJExecutors;
import jj.execution.JJRunnable;
import jj.execution.ScriptThread;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
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
public class DocumentRequestProcessorImpl implements DocumentRequestProcessor {
	
	@SuppressWarnings("serial")
	private static class FilterList extends ArrayList<DocumentFilter> {}
	
	/** the executors in which we run */
	final JJExecutors executors;
	
	private final HtmlResource resource;
	
	private final Document document;
	
	private final HttpRequest httpRequest;
	
	private final HttpResponse httpResponse;
	
	private final Set<DocumentFilter> filters;
	
	private ArrayList<JJMessage> messages; 
	
	private AssociatedScriptBundle associatedScriptBundle;
	
	private volatile DocumentRequestState state = DocumentRequestState.Uninitialized;

	public DocumentRequestProcessorImpl(
		final JJExecutors executors,
		final HtmlResource resource,
		final HttpRequest httpRequest,
		final HttpResponse httpResponse,
		final Set<DocumentFilter> filters
	) {
		this.executors = executors;
		this.resource = resource;
		this.document = resource.document().clone();
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
		return resource.baseName();
	}
	
	@Override
	public void process() {
		executors.scriptRunner().submit(this);
	}
	
	/**
	 * Pulls the document together and spits it out
	 */
	@ScriptThread
	public void respond() {
		assert executors.isScriptThreadFor(baseName()) : "must be called in a script thread for " + baseName();
		
		executeFilters(makeFilterList(filters, false));
		
		final FilterList ioFilters = makeFilterList(filters, true);
		
		if (ioFilters.isEmpty()) {
			writeResponse();
		} else {
			executors.ioExecutor().submit(new JJRunnable("Document filtering requiring I/O") {
				
				@Override
				public void run() {
					executeFilters(ioFilters);
					writeResponse();
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
		try {
			httpResponse
				.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length)
				// clients shouldn't cache these responses at all
				.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
				.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
				.content(bytes)
				.end();
			
		} catch (Exception e) {
			httpResponse.error(e);
		}
	}
	
	@Override
	public String toString() {
		return httpRequest.uri();
	}
	

	@Override
	public DocumentRequestProcessor startingInitialExecution() {
		state = DocumentRequestState.InitialExecution;
		associatedScriptBundle().initializing(true);
		return this;
	}
	
	
	@Override
	public DocumentRequestProcessor startingReadyFunction() {
		state = DocumentRequestState.ReadyFunctionExecution;
		return this;
	}
	
	@Override
	public DocumentRequestState state() {
		return state;
	}
	


	/**
	 * adds a message intended to be processed a framework startup
	 * on the client.  initially intended for event bindings but
	 * some other case may come up
	 * @param message
	 */
	@Override
	public DocumentRequestProcessor addStartupJJMessage(final JJMessage message) {
		if (messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(message);
		return this;
	}

	@Override
	public List<JJMessage> startupJJMessages() {
		ArrayList<JJMessage> messages = this.messages;
		this.messages = null;
		return messages == null ? Collections.<JJMessage>emptyList() : messages;
	}
	

	@Override
	public AssociatedScriptBundle associatedScriptBundle() {
		return associatedScriptBundle;
	}

	@Override
	public DocumentRequestProcessor associatedScriptBundle(AssociatedScriptBundle associatedScriptBundle) {
		this.associatedScriptBundle = associatedScriptBundle;
		return this;
	}
	
	@Override
	public String uri() {
		return httpRequest.uri();
	}
}
