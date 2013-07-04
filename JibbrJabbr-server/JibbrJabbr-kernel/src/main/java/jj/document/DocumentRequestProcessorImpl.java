package jj.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Set;

import jj.JJExecutors;
import jj.JJRunnable;
import jj.ScriptThread;
import jj.script.AssociatedScriptBundle;
import jj.http.JJHttpRequest;

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
	
	/** the document request */
	private final DocumentRequest documentRequest;
	
	private final Set<DocumentFilter> filters;

	public DocumentRequestProcessorImpl(
		final JJExecutors executors,
		final DocumentRequest documentRequest,
		final Set<DocumentFilter> filters
	) {
		this.executors = executors;
		this.documentRequest = documentRequest;
		this.filters = filters;
	}
	
	private FilterList makeFilterList(final Set<DocumentFilter> filters, final boolean io) {
		FilterList filterList = new FilterList();
		for (DocumentFilter filter: filters) {
			if (filter.needsIO(documentRequest) && io || 
				(!filter.needsIO(documentRequest) && !io)) {
				filterList.add(filter);
			}
		}
		return filterList;
	}
	
	public JJHttpRequest httpRequest() {
		return documentRequest.httpRequest();
	}
	
	public AssociatedScriptBundle associatedScriptBundle() {
		return documentRequest.httpRequest().associatedScriptBundle();
	}
	
	public void scriptBundle(AssociatedScriptBundle scriptBundle) {
		documentRequest.httpRequest().associatedScriptBundle(scriptBundle);
	}
	
	public Document document() {
		return documentRequest.document();
	}
	
	public String baseName() {
		return documentRequest.baseName();
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
		assert executors.isScriptThread() : "must be called in a script thread";
		
		executeFilters(makeFilterList(filters, false));
		
		final FilterList ioFilters = makeFilterList(filters, true);
		
		if (ioFilters.isEmpty()) {
			writeResponse();
		} else {
			executors.ioExecutor().submit(executors.prepareTask(new JJRunnable("Document filtering requiring I/O") {
				
				@Override
				public void run() throws Exception {
					executeFilters(ioFilters);
					writeResponse();
				}
			}));
		}
	}
	
	private void executeFilters(final FilterList filterList) {
		for (DocumentFilter filter : filterList) {
			filter.filter(documentRequest);
		}
	}

	private void writeResponse() {
		// pretty printing is turned off because it inserts weird spaces
		// into the output if there are text nodes next to element node
		// and it gets REALLY ANNOYING
		documentRequest.document().outputSettings().prettyPrint(false).indentAmount(0);
		byte[] bytes = documentRequest.document().toString().getBytes(UTF_8);
		try {
			documentRequest.httpResponse()
				.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length)
				// clients shouldn't cache these responses at all
				.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
				.header(HttpHeaders.Names.CONTENT_TYPE, documentRequest.mime())
				.content(bytes)
				.end();
			
		} catch (Exception e) {
			documentRequest.httpResponse().error(e);
		}
	}
	
	@Override
	public String toString() {
		return httpRequest().uri();
	}
}
