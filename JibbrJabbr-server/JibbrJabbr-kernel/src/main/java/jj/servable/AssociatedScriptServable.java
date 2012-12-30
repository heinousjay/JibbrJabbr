package jj.servable;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;

import jj.resource.ScriptResource;
import jj.script.ScriptBundle;
import jj.script.ScriptBundleFinder;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

/**
 * handles automatic inclusion
 * @author jason
 *
 */
class AssociatedScriptServable extends Servable {
	
	/**
	 * twenty years in seconds.  not including leap days. it's probably fine
	 */
	private static final String TWENTY_YEARS = String.valueOf(60 * 60 * 24 * 365 * 20);
	
	private final ScriptBundleFinder finder;

	AssociatedScriptServable(final Path basePath, final ScriptBundleFinder finder) {
		super(basePath);
		this.finder = finder;
	}
	
	@Override
	protected Rank rank() {
		return Rank.Middle;
	}
	
	private ScriptResource typeFromBundle(ScriptBundle bundle, String typeSpec) {
		ScriptResource result = null;
		if ("".equals(typeSpec)) {
			result = bundle.clientScriptResource();
		} else if (".shared".equals(typeSpec)) {
			result = bundle.sharedScriptResource();
		} else if (".server".equals(typeSpec)) {
			result = bundle.serverScriptResource();
		}
		return result;
	}
	
	private ScriptResource resourceFromUri(String uri) {
		int firstDot = uri.indexOf('.');
		int lastDot = uri.lastIndexOf('.');
		ScriptResource result = null;
		if (firstDot != -1) {
			// skip the /
			String key = uri.substring(1, firstDot);
			String type = uri.substring(firstDot, lastDot);
			String suffix = uri.substring(lastDot);
			if (".js".equals(suffix)) {
				ScriptBundle scriptBundle = finder.forBaseNameAndKey(key);
				if (scriptBundle != null) {
					result = typeFromBundle(scriptBundle, type);
				}
			}
		}
		return result;
	}

	@Override
	public boolean isMatchingRequest(final JJHttpRequest request) {
		return resourceFromUri(request.uri()) != null;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final HttpResponse response,
		final HttpControl control
	) throws IOException {
		
		final ScriptResource scriptResource = resourceFromUri(request.uri());
		// we can serve this one inline for now
		return new RequestProcessor() {
			
			@Override
			public void process() {
				response.charset(UTF_8)
					.header(HttpHeaders.Names.CACHE_CONTROL, "max-age=" + TWENTY_YEARS)
					.header(HttpHeaders.Names.CONTENT_TYPE, "text/javascript; charset=UTF-8")
					.content(scriptResource.script())
					.end();
			}
		};
	}
}
