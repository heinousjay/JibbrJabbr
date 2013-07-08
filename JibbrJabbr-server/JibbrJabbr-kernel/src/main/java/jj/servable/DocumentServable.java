package jj.servable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.document.DocumentFilter;
import jj.document.DocumentRequest;
import jj.document.DocumentRequestProcessorImpl;
import jj.execution.JJExecutors;
import jj.resource.HtmlResource;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;
import jj.uri.URIMatch;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.RequestProcessor;

@Singleton
class DocumentServable extends Servable {
	
	public static final String SLASH = "/";
	public static final String DOT = ".";
	public static final String HTML = "html";
	public static final String DOT_HTML = DOT + HTML;
	public static final String INDEX = "index" + DOT_HTML;
	
	private final ResourceFinder resourceFinder;
	private final JJExecutors executors;
	private final Set<DocumentFilter> documentFilters;
	
	@Inject
	DocumentServable(
		final Configuration configuration,
		final ResourceFinder resourceFinder, 
		final JJExecutors executors,
		final Set<DocumentFilter> documentFilters
	) {
		super(configuration);
		this.resourceFinder = resourceFinder;
		this.executors = executors;
		this.documentFilters = documentFilters;
	}
	
	@Override
	public boolean isMatchingRequest(final HttpRequest request) {
		return request.uri().endsWith(DOT_HTML) ||
			request.uri().endsWith(SLASH) ||
			request.uri().lastIndexOf(DOT) <= request.uri().lastIndexOf(SLASH);
	}
	
	private Path toPath(final HttpRequest request) {
		URIMatch match = new URIMatch(request.uri());
		Path result = null;
		String uri = match.baseName;
		
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
		Path realPath = basePath.relativize(path);
		String baseName = realPath.toString();
		baseName = baseName.substring(0, baseName.length() - DOT_HTML.length());
		return baseName;
	}
	
	private void ensureScriptPreload(final String baseName) {
		if (executors.isIOThread()) {
			// since we're in the IO thread already and we might need this stuff soon, as a small
			// optimization to avoid jumping right back into the I/O thread after dispatching this
			// into the script thread, we just "prime the pump"
			resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Client);
			resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Shared);
			resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Server);
		}
	}
	
	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		
		DocumentRequestProcessorImpl result = null;
		Path path = toPath(request);
		
		if (path != null && isServablePath(path)) {
			
			String baseName = toBaseName(path);
			
			HtmlResource htmlResource = 
				executors.isIOThread() ?
				resourceFinder.loadResource(HtmlResource.class, baseName) :
				resourceFinder.findResource(HtmlResource.class, baseName);
			
			if (htmlResource != null) {
			
				ensureScriptPreload(baseName);
				
				result = new DocumentRequestProcessorImpl(
					executors,
					new DocumentRequest(htmlResource, htmlResource.document(), request, response, executors.isIOThread()),
					documentFilters
				);
			}
		}
		
		return result;
	}
}
