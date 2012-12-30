package jj.servable;

import java.io.IOException;
import java.nio.file.Path;

import jj.JJExecutors;
import jj.document.DocumentFilter;
import jj.document.DocumentRequest;
import jj.document.DocumentRequestProcessorImpl;
import jj.resource.HtmlResource;
import jj.resource.ResourceFinder;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

class HtmlServable extends Servable {
	
	public static final String SLASH = "/";
	public static final String DOT = ".";
	public static final String DOT_HTML = DOT + "html";
	public static final String INDEX = "index" + DOT_HTML;
	
	private final ResourceFinder resourceFinder;
	private final JJExecutors executors;
	private final DocumentFilter[] documentFilters;
	
	HtmlServable(
		final Path basePath,
		final ResourceFinder resourceFinder, 
		final JJExecutors executors,
		final DocumentFilter[] documentFilters
	) {
		super(basePath);
		this.resourceFinder = resourceFinder;
		this.executors = executors;
		this.documentFilters = documentFilters;
	}
	
	@Override
	protected Rank rank() {
		return Rank.First;
	}
	
	@Override
	public boolean needsIO(final JJHttpRequest request) {
		return resourceFinder.findResource(HtmlResource.class, toBaseName(toPath(request))) == null;
	}
	
	@Override
	public boolean isMatchingRequest(final JJHttpRequest request) {
		return request.uri().endsWith(DOT_HTML) ||
			request.uri().endsWith(SLASH) ||
			request.uri().lastIndexOf(DOT) <= request.uri().lastIndexOf(SLASH);
	}
	
	private Path toPath(final JJHttpRequest request) {
		String uri = request.uri().substring(1);
		Path result = null;
		if (uri.endsWith(DOT_HTML)) {
			result = basePath.resolve(uri).normalize();
		} else if (uri.endsWith(SLASH)) {
			result = basePath.resolve(uri).resolve(INDEX).normalize();
		} else if ("".equals(uri)) {
			result = basePath.resolve(INDEX);
		} else {
			result = basePath.resolve(uri + DOT_HTML).normalize();
		}
		
		if (!isServablePath(result)) {
			result = null;
		}
		
		return result;
	}
	
	private String toBaseName(final Path path) {
		String baseName = basePath.relativize(path).toString();
		baseName = baseName.substring(0, baseName.length() - DOT_HTML.length());
		return baseName;
	}
	
	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final HttpResponse response,
		final HttpControl control
	) throws IOException {
		DocumentRequestProcessorImpl result = null;
		Path path = toPath(request);
		if (path != null) {
			String baseName = toBaseName(path);
			HtmlResource htmlResource = 
				executors.isIOThread() ?
				resourceFinder.loadResource(HtmlResource.class, baseName) :
				resourceFinder.findResource(HtmlResource.class, baseName);
			if (htmlResource != null) {
				result = new DocumentRequestProcessorImpl(
					executors,
					new DocumentRequest(htmlResource, htmlResource.document(), request, response, control),
					documentFilters
				);
			}
		}
		return result;
	}
}
