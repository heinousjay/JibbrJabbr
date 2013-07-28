package jj.http.server.servable;

import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.configuration.Configuration;
import jj.execution.IOThread;
import jj.resource.HtmlResource;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;
import jj.uri.URIMatch;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.document.DocumentRequestProcessor;

@Singleton
class DocumentServable extends Servable {
	
	public static final String SLASH = "/";
	public static final String DOT = ".";
	public static final String HTML = "html";
	public static final String DOT_HTML = DOT + HTML;
	public static final String INDEX = "index" + DOT_HTML;
	
	private final ResourceFinder resourceFinder;
	private final Injector parentInjector;
	
	@Inject
	DocumentServable(
		final Configuration configuration,
		final ResourceFinder resourceFinder,
		final Injector parentInjector
	) {
		super(configuration);
		this.resourceFinder = resourceFinder;
		this.parentInjector = parentInjector;
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
		// since we're in the IO thread already and we might need this stuff soon, as a small
		// optimization to avoid jumping right back into the I/O thread after dispatching this
		// into the script thread, we just "prime the pump"
		resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Client);
		resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Shared);
		resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Server);
	}
	
	@Override
	@IOThread
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		
		RequestProcessor result = null;
		Path path = toPath(request);
		
		if (path != null) {
			
			String baseName = toBaseName(path);
			
			final HtmlResource htmlResource = resourceFinder.loadResource(HtmlResource.class, baseName);
			
			if (htmlResource != null) {
			
				ensureScriptPreload(baseName);
				
				result = parentInjector.createChildInjector(new AbstractModule() {
					
					@Override
					protected void configure() {
						bind(HtmlResource.class).toInstance(htmlResource);
						bind(HttpRequest.class).toInstance(request);
						bind(HttpResponse.class).toInstance(response);
						bind(RequestProcessor.class).to(DocumentRequestProcessor.class);
					}
				}).getInstance(RequestProcessor.class);
			}
		}
		
		return result;
	}
}
