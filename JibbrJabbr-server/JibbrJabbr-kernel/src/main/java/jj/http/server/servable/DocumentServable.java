package jj.http.server.servable;

import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.configuration.Configuration;
import jj.execution.IOThread;
import jj.resource.ResourceFinder;
import jj.resource.asset.AssetResource;
import jj.resource.html.HtmlResource;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.resource.script.environment.DocumentScriptEnvironment;
import jj.uri.URIMatch;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.document.DocumentRequestProcessor;

@Singleton
class DocumentServable extends Servable<HtmlResource> {
	
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
	public boolean isMatchingRequest(final URIMatch uriMatch) {
		return HTML.equals(uriMatch.extension) ||
			uriMatch.uri.endsWith(SLASH) ||
			uriMatch.uri.lastIndexOf(DOT) <= uriMatch.uri.lastIndexOf(SLASH);
	}
	
	private Path toPath(final HttpRequest request) {
		URIMatch match = new URIMatch(request.uri());
		Path result = null;
		String uri = match.baseName;
		
		if (uri.endsWith(DOT_HTML)) {
			result = appPath().resolve(uri).normalize();
		} else if (uri.endsWith(SLASH)) {
			result = appPath().resolve(uri).resolve(INDEX).normalize();
		} else if ("".equals(uri)) {
			result = appPath().resolve(INDEX);
		} else {
			result = appPath().resolve(uri + DOT_HTML).normalize();
		}
		
		if (!isServablePath(result)) {
			result = null;
		}
		
		return result;
	}
	
	private String toBaseName(final Path path) {
		Path realPath = appPath().relativize(path);
		String baseName = realPath.toString();
		baseName = baseName.substring(0, baseName.length() - DOT_HTML.length());
		return baseName;
	}
	
	private void preloadResources(final String baseName) {
		// since we're in the IO thread already and we might need this stuff soon, as a small
		// optimization to avoid jumping right back into the I/O thread after dispatching this
		// into the script thread, we just "prime the pump"
		resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Client.suffix(baseName));
		resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Shared.suffix(baseName));
		resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		resourceFinder.loadResource(AssetResource.class, AssetResource.JJ_JS);
		resourceFinder.loadResource(AssetResource.class, AssetResource.JQUERY_JS);
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
			
			resourceFinder.loadResource(DocumentScriptEnvironment.class, baseName);
			
			final HtmlResource htmlResource = loadResource(new URIMatch("/" + baseName));
			
			if (htmlResource != null) {
			
				preloadResources(baseName);
				
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

	@Override
	public HtmlResource loadResource(URIMatch match) {
		return resourceFinder.loadResource(HtmlResource.class, match.baseName);
	}

	@Override
	public Class<HtmlResource> type() {
		return HtmlResource.class;
	}
}
