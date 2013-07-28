package jj.engine;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import jj.http.client.HttpClient;
import jj.http.client.JJHttpClientRequest;
import jj.script.CurrentScriptContext;
import jj.script.RestRequest;
import jj.uri.Route;

import io.netty.handler.codec.http.HttpHeaders;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class RestCallProvider {
	
	private final Logger log = LoggerFactory.getLogger(RestCallProvider.class);
	
	private final HttpClient httpClient;
	
	private final CurrentScriptContext context;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private class RestCall extends BaseFunction {
		private static final long serialVersionUID = 1L;
		
		private final RestCallOptions options;
		
		private RestCall(final RestCallOptions options) {
			this.options = options;
		}
		
		private String baseUrl(Scriptable thisObj) {
			return (String)Context.jsToJava(ScriptableObject.getProperty(thisObj, "baseUrl"), String.class);
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			
			Map<String, Object> params = new HashMap<>();
			
			if (options.params() != null) {
				for (String key : options.params().keySet()) {
					params.put(key, options.params().get(key));
				}
			}
			
			if (args.length == 1 && args[0] instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> merge = (Map<String, Object>)args[0];
				for (String key : merge.keySet()) {
					params.put(key, merge.get(key));
				}
			} else if (args.length != 0) {
				throw new IllegalArgumentException("service functions take an object parameter or no parameters");
			}
			
			String body = null;
			StringBuilder url = new StringBuilder(baseUrl(thisObj));
			
			// we need to JSON serialize this.  yay
			if (!params.isEmpty()) {
				Route route = new Route(options.path());
				// one at a time, because we need to do string conversions here
				Map<String, String> routeParams = new HashMap<>();
				for (String key : params.keySet()) {
					String param = (String)Context.jsToJava(params.get(key), String.class);
					routeParams.put(key, param);
				}
				
				url.append(route.generate(routeParams));
				
				// then if anything is left
				// that gets consumed according to what we produce
				switch (options.produce()) {
				case JSON:
					if (!params.isEmpty()) {
						try {
							body = objectMapper.writeValueAsString(params);
						} catch (JsonProcessingException e) {
							throw new IllegalArgumentException("service call parameter couldn't be serialized", e);
						}
					}
					break;
				case UrlEncoded:
					// if anything is left here, we ignore it? 
					// or splat it onto the end of the url as parameters?
					break;
				}
			} else {
				url.append(options.path());
			}
			
			final JJHttpClientRequest request = 
				new JJHttpClientRequest(options.method(), url.toString())
				.header(HttpHeaders.Names.ACCEPT, options.accept().toString());
			
			if (body != null && !"".equals(body.trim())) {
				byte[] bytes = UTF_8.encode(body).array();
				
				request
					.header(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8")
					.header(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(bytes.length));
					// TODO!
					//.setBody(bytes);
			}
			
			log.debug("performing REST request {}", request);

			if (!options.ignoreResult()) {
				// TODO - handle the result in a way consistent with configuration, gets set on the RestRequest
				throw context.prepareContinuation(new RestRequest(request));
			} else {
				// just fire and forget!
				httpClient.execute(request);
			}
			
			
			return Undefined.instance;
		}
		
		@Override
		public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
			throw new UnsupportedOperationException("cannot construct RestCalls");
		}
	}
	
	@Inject
	RestCallProvider(
		final HttpClient httpClient,
		final CurrentScriptContext context
	) {
		this.httpClient = httpClient;
		this.context = context;
	}
	
	Function createRestCall(final RestCallOptions options) {
		return new RestCall(options);
	}
}
