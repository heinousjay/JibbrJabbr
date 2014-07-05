package jj.http.server.servable;

import io.netty.handler.codec.http.HttpHeaders;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.resolution.AppLocation;
import jj.document.DocumentScriptEnvironment;
import jj.resource.PathResolver;
import jj.resource.ResourceFinder;
import jj.script.module.ScriptResource;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.http.server.uri.URIMatch;

/**
 * handles serving the scripts associated with a document
 * request.  A special SHA key is generated to version a
 * particular group of scripts according to an execution environment.
 * @author jason
 *
 */
@Singleton
class DocumentScriptServable extends Servable<ScriptResource> {
	
	private final ResourceFinder resourceFinder;

	@Inject
	DocumentScriptServable(final PathResolver pathResolver, final ResourceFinder finder) {
		super(pathResolver);
		this.resourceFinder = finder;
	}
	
	private ScriptResource typeFromExecutionEnvironment(DocumentScriptEnvironment executionEnvironment, String typeSpec) {
		ScriptResource result = null;
		if (typeSpec == null) {
			result = executionEnvironment.clientScriptResource();
		} else if ("shared".equals(typeSpec)) {
			result = executionEnvironment.sharedScriptResource();
		} 
		return result;
	}
	
	private static final Pattern TYPE_PATTERN = Pattern.compile("(.+?)(?:\\.(server|shared))?\\.js");
	
	@Override
	public boolean isMatchingRequest(final URIMatch uriMatch) {
		return loadResource(uriMatch) != null;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpServerRequest request,
		final HttpServerResponse response
	) throws IOException {
		final ScriptResource script = loadResource(request.uriMatch());
		
		return script == null ? null : new RequestProcessor() {
			
			@Override
			public void process() throws IOException {
				
				if (request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH) &&
					script.sha1().equals(request.header(HttpHeaders.Names.IF_NONE_MATCH))) {
					
					response.sendNotModified(script, true);
					
				} else {
				
					response.sendCachedResource(script);
				}
			}
		};
	}

	@Override
	public ScriptResource loadResource(final URIMatch match) {
		ScriptResource result = null;
		Matcher typeMatcher = TYPE_PATTERN.matcher(match.baseName);
		if (match.sha1 != null && typeMatcher.matches()) {
			
			DocumentScriptEnvironment scriptEnvironment = 
				resourceFinder.findResource(DocumentScriptEnvironment.class, AppLocation.Virtual, typeMatcher.group(1));
			if (scriptEnvironment != null) {
				result = typeFromExecutionEnvironment(scriptEnvironment, typeMatcher.group(2));
			}
		}
		
		return result;
	}
}
