package jj.http.server.servable;

import io.netty.handler.codec.http.HttpHeaders;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.ScriptResource;
import jj.script.DocumentScriptExecutionEnvironment;
import jj.script.ScriptExecutionEnvironmentFinder;
import jj.uri.URIMatch;
import jj.http.HttpRequest;
import jj.http.HttpResponse;

/**
 * handles serving the scripts associated with a document
 * request.  A special SHA key is generated to version a
 * particular group of scripts according to an execution environment.
 * @author jason
 *
 */
@Singleton
class DocumentScriptServable extends Servable<ScriptResource> {
	
	private final ScriptExecutionEnvironmentFinder finder;

	@Inject
	DocumentScriptServable(final Configuration configuration, final ScriptExecutionEnvironmentFinder finder) {
		super(configuration);
		this.finder = finder;
	}
	
	private ScriptResource typeFromExecutionEnvironment(DocumentScriptExecutionEnvironment executionEnvironment, String typeSpec) {
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
		final HttpRequest request,
		final HttpResponse response
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
			
			DocumentScriptExecutionEnvironment scriptExecutionEnvironment = finder.forURIMatch(match);
			if (scriptExecutionEnvironment != null) {
				result = typeFromExecutionEnvironment(scriptExecutionEnvironment, typeMatcher.group(2));
			}
		}
		
		return result;
	}

	@Override
	public Class<ScriptResource> type() {
		return ScriptResource.class;
	}
}
