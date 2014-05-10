package jj.http.server.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.configuration.resolution.AppLocation;
import jj.configuration.resolution.Application;
import jj.configuration.resolution.Assets;
import jj.document.DocumentScriptEnvironment;
import jj.document.HtmlResource;
import jj.document.servable.DocumentRequestProcessor;
import jj.resource.ResourceThread;
import jj.resource.ResourceFinder;
import jj.resource.stat.ic.StaticResource;
import jj.uri.URIMatch;
import jj.util.StringUtils;
import jj.http.server.HttpRequest;
import jj.http.server.HttpResponse;

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
		final Application app,
		final ResourceFinder resourceFinder,
		final Injector parentInjector
	) {
		super(app);
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
		resourceFinder.loadResource(StaticResource.class, AppLocation.Assets, Assets.JJ_JS);
		resourceFinder.loadResource(StaticResource.class, AppLocation.Assets, Assets.JQUERY_JS);
	}
	
	@Override
	@ResourceThread
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		
		RequestProcessor result = null;
		
		String baseName = request.uriMatch().baseName;
		
		if (StringUtils.isEmpty(request.uriMatch().extension)) {
			
			final DocumentScriptEnvironment dse = 
				resourceFinder.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, baseName);
			
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
		return resourceFinder.loadResource(HtmlResource.class, AppLocation.Base, match.baseName);
	}
}
