package jj.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import jj.HttpControlThread;
import jj.IOThread;
import jj.JJExecutors;
import jj.JJRunnable;
import jj.ScriptThread;
import jj.script.ScriptBundle;
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
	
	private final DocumentFilter[] filters;
	

	@IOThread
	public DocumentRequestProcessorImpl(
		final JJExecutors executors,
		final DocumentRequest documentRequest,
		final DocumentFilter[] filters
	) {
		this.executors = executors;
		this.documentRequest = documentRequest;
		this.filters = filters;
	}
	
	private FilterList makeFilterList(final DocumentFilter[] filters, final boolean io) {
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
	
	public ScriptBundle scriptBundle() {
		return documentRequest.httpRequest().scriptBundle();
	}
	
	public void scriptBundle(ScriptBundle scriptBundle) {
		documentRequest.httpRequest().scriptBundle(scriptBundle);
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
			String taskName = String.format("DocumentFilter%s", (executors.isIOThread() ? " w/IO" : ""));
			JJRunnable r = new JJRunnable(taskName) {
				@Override
				public void innerRun() throws Exception {
					ioFilters.get(index).filter(documentRequest);
					dispatchIONextFilter(ioFilters, currentIOFilter);
				}
			};
			if (executors.isIOThread()) {
				r.run();
			} else {
				executors.ioExecutor().submit(r);
			}
		}
	}

	private void writeResponse() {
		documentRequest.httpControl().execute(new JJRunnable("Writing HTTP response") {
			
			@Override
			@HttpControlThread
			public void innerRun() throws Exception {
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
		});
	}
}
