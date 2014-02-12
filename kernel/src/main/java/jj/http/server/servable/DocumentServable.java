package jj.http.server.servable;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.StringUtils;
import jj.configuration.Configuration;
import jj.execution.IOThread;
import jj.resource.ResourceFinder;
import jj.resource.asset.AssetResource;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.document.HtmlResource;
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
		return uriMatch.uri.endsWith(SLASH) ||
			uriMatch.uri.lastIndexOf(DOT) <= uriMatch.uri.lastIndexOf(SLASH);
	}
	
	private void preloadResources(final String baseName) {
		// since we're in the IO thread already and we might need this stuff soon, as a small
		// optimization to avoid jumping right back into the I/O thread after dispatching this
		// into the script thread, we just "prime the pump"
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
		
		String baseName = request.uriMatch().baseName;
		
		if (StringUtils.isEmpty(request.uriMatch().extension)) {
			
			final DocumentScriptEnvironment dse = 
				resourceFinder.loadResource(DocumentScriptEnvironment.class, baseName);
			
			if (dse != null) {
				
				preloadResources(baseName);
			
				result = parentInjector.createChildInjector(new AbstractModule() {
					
					@Override
					protected void configure() {
						bind(DocumentScriptEnvironment.class).toInstance(dse);
						bind(HttpRequest.class).toInstance(request);
						bind(HttpResponse.class).toInstance(response);
					}
				}).getInstance(DocumentRequestProcessor.class);
			}
		}
		
		return result;
	}

	@Override
	public HtmlResource loadResource(URIMatch match) {
		return resourceFinder.loadResource(HtmlResource.class, match.baseName);
	}
}
