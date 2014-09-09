package jj.http.server.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.configuration.resolution.AppLocation;
import jj.configuration.resolution.Assets;
import jj.document.DocumentScriptEnvironment;
import jj.document.servable.DocumentRequestProcessor;
import jj.resource.ResourceThread;
import jj.resource.ResourceFinder;
import jj.resource.stat.ic.StaticResource;
import jj.util.StringUtils;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.http.server.uri.URIMatch;

@Singleton
class DocumentServable extends Servable<DocumentScriptEnvironment> {
	
	public static final String SLASH = "/";
	public static final String DOT = ".";
	public static final String HTML = "html";
	public static final String DOT_HTML = DOT + HTML;
	public static final String INDEX = "index" + DOT_HTML;
	
	private final ResourceFinder resourceFinder;
	private final Injector parentInjector;
	
	@Inject
	DocumentServable(
		final ResourceFinder resourceFinder,
		final Injector parentInjector
	) {
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
		final HttpServerRequest request,
		final HttpServerResponse response
	) throws IOException {
		
		RequestProcessor result = null;
		
		if (StringUtils.isEmpty(request.uriMatch().extension)) {
			
			final DocumentScriptEnvironment dse = loadResource(request.uriMatch());
			if (dse != null) {
				
				if (dse.initializationDidError()) {
					
					response.error(dse.initializationError());
					
				} else {
					
					preloadResources(request.uriMatch().path);
				
					result = parentInjector.createChildInjector(new AbstractModule() {
						
						@Override
						protected void configure() {
							bind(DocumentScriptEnvironment.class).toInstance(dse);
							bind(HttpServerRequest.class).toInstance(request);
							bind(HttpServerResponse.class).toInstance(response);
						}
					}).getInstance(DocumentRequestProcessor.class);
				}
			}
		}
		
		return result;
	}

	@Override
	public DocumentScriptEnvironment loadResource(URIMatch match) {
		return resourceFinder.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, match.path);
	}
}
