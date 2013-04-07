package jj.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jj.HttpControlThread;
import jj.JJExecutors;
import jj.JJRunnable;
import jj.ScriptThread;
import jj.script.AssociatedScriptBundle;
import jj.webbit.JJHttpRequest;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates the resources necessary to execute a request
 * to serve an HTML5 Document, and hands the result back to
 * webbit
 * @author jason
 *
 */
public class DocumentRequestProcessorImpl implements DocumentRequestProcessor {
	
	@SuppressWarnings("serial")
	private static class FilterList extends ArrayList<DocumentFilter> {}
	
	private final Logger log = LoggerFactory.getLogger(DocumentRequestProcessorImpl.class);
	
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
		
		executeScriptFilters(makeFilterList(filters, false));
		// the response gets written when this complete
		FilterList ioFilters = makeFilterList(filters, true);
		dispatchIONextFilter(ioFilters, new AtomicInteger(0));
	}
	
	private void executeScriptFilters(final FilterList scriptFilters) {
		// well this one is easy
		for (DocumentFilter filter : scriptFilters) {
			filter.filter(documentRequest);
		}
	}
	
	private void dispatchIONextFilter(final FilterList ioFilters, final AtomicInteger currentIOFilter) {
		// this is slightly more complicated - we need to
		// ensure strict ordering of filter execution because
		// we don't want to synchronize on the document,
		// so we dispatch into an IO thread and loop from there
		// using an AtomicInteger because we need volatile semantics
		final int index = currentIOFilter.getAndIncrement();
		if (index >= ioFilters.size()) {
			writeResponse();
		} else {
			final String taskName = String.format("DocumentFilter %s", (executors.isIOThread() ? " w/IO" : ""));
			Runnable r = executors.prepareTask(new JJRunnable(taskName) {
				
				@Override
				public void run() throws Exception {
					// not asserting IO thread here since this is only called from the next few lines
					ioFilters.get(index).filter(documentRequest);
					dispatchIONextFilter(ioFilters, currentIOFilter);
				}
			});
			if (executors.isIOThread()) {
				r.run();
			} else {
				executors.ioExecutor().submit(r);
			}
		}
	}

	private void writeResponse() {
		executors.httpControlExecutor().execute(executors.prepareTask(new JJRunnable("Writing HTTP response") {
			
			@Override
			@HttpControlThread
			public void run() throws Exception {
				// pretty printing is turned off because it inserts weird spaces
				// into the output if there are text nodes next to element node
				// and it gets REALLY ANNOYING
				documentRequest.document().outputSettings().prettyPrint(false);
				final ByteBuffer bytes = UTF_8.encode(documentRequest.document().toString());
				try {
					documentRequest.httpResponse()
						.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.remaining())
						// clients shouldn't cache these responses at all
						.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
						.header(
							HttpHeaders.Names.CONTENT_TYPE, 
							documentRequest.mime()
						)
						.content(bytes)
						.end();
					
				} catch (Exception e) {
					log.error("error responding to {}", documentRequest.httpRequest().uri());
					throw e;
				}
				
				log.info(
					"request for [{}] completed in {} milliseconds (wall time)",
					documentRequest.httpRequest().uri(),
					documentRequest.httpRequest().wallTime()
				);
			}
		}));
	}
}
