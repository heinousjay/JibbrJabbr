package jj.servable;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.ScriptResource;
import jj.script.AssociatedScriptBundle;
import jj.script.ScriptBundleFinder;
import jj.webbit.JJHttpRequest;
import jj.webbit.JJHttpResponse;
import jj.webbit.RequestProcessor;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;

/**
 * handles automatic inclusion
 * @author jason
 *
 */
@Singleton
class AssociatedScriptServable extends Servable {
	
	private final Logger log = LoggerFactory.getLogger(AssociatedScriptServable.class);
	
	private final ScriptBundleFinder finder;

	@Inject
	AssociatedScriptServable(final Configuration configuration, final ScriptBundleFinder finder) {
		super(configuration);
		this.finder = finder;
	}
	
	private ScriptResource typeFromBundle(AssociatedScriptBundle bundle, String typeSpec) {
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
				AssociatedScriptBundle scriptBundle = finder.forBaseNameAndKey(key);
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
		final JJHttpResponse response,
		final HttpControl control
	) throws IOException {
		
		final ScriptResource scriptResource = resourceFromUri(request.uri());
		// we can serve this one inline for now
		return new RequestProcessor() {
			
			@Override
			public void process() {
				
				ByteBuffer buf = UTF_8.encode(scriptResource.script());
				
				response.header(HttpHeaders.Names.CONTENT_LENGTH, buf.remaining())
					.header(HttpHeaders.Names.CACHE_CONTROL, TWENTY_YEARS)
					.header(HttpHeaders.Names.CONTENT_TYPE, scriptResource.mime())
					.content(buf)
					.end();

				
				log.info(
					"request for [{}] completed in {} milliseconds (wall time)",
					request.uri(),
					request.wallTime()
				);
			}
		};
	}
}
