package jj.hostapi;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import jj.script.CurrentScriptContext;
import jj.script.RestRequest;
import jj.uri.UriTemplate;

import org.jboss.netty.handler.codec.http.HttpHeaders;
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
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

class RestCallProvider {
	
	private final Logger log = LoggerFactory.getLogger(RestCallProvider.class);
	
	private final AsyncHttpClient httpClient;
	
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
			
			
			
			Scriptable params = cx.newObject(scope);
			
			if (options.params() != null) {
				for (Object idObj : options.params().getIds()) {
					String id = (String)idObj;
					params.put(id, params, options.params().get(id, options.params()));
				}
			}
			
			if (args.length == 1 && args[0] instanceof Scriptable) {
				Scriptable merge = (Scriptable)args[0];
				for (Object idObj : merge.getIds()) {
					String id = (String)idObj;
					params.put(id, params, merge.get(id, merge));
				}
			}
			
			String body = null;
			StringBuilder url = new StringBuilder(baseUrl(thisObj));
			
			// we need to JSON serialize this.  yay
			if (params.getIds().length > 0) {
				UriTemplate path = new UriTemplate(options.path());
				
				for (Object idObj : params.getIds()) {
					String id = String.valueOf(idObj);
					String param = (String)Context.jsToJava(params.get(id, params), String.class);
					path.set(id, param);
				}
				
				url.append(path.expand());
				
				// delete all matches
				// then if anything is left
				// that get consumed according to what we produce
				switch (options.produce()) {
				case JSON:
					if (params.getIds().length > 0) {
						try {
							body = objectMapper.writeValueAsString(params);
						} catch (JsonProcessingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// BAD
						}
					}
					break;
				case UrlEncoded:
					// ugh.
					break;
				}
			} else {
				url.append(options.path());
			}
			
			final RequestBuilder requestBuilder = new RequestBuilder(options.method().toString())
				.setUrl(url.toString())
				.addHeader("Accept", options.accept().toString());
			
			if (body != null && !"".equals(body.trim())) {
				byte[] bytes = UTF_8.encode(body).array();
				
				requestBuilder
					.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8")
					.addHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(bytes.length))
					.setBody(bytes);
			}
				
			
			
			final Request request = requestBuilder.build();
			
			log.debug("performing REST request {}", request);

			if (!options.ignoreResult()) {
				// TODO - handle the result in a way consistent with configuration, gets set on the RestRequest
				throw context.prepareContinuation(new RestRequest(request));
			} else {
				// just fire and forget!
				try {
					httpClient.executeRequest(request);
				} catch (IOException e) {
					log.error("executing a REST request went poorly", e);
					// TODO probably need to throw something here, no?
					// something more direct and intelligent of course
					throw new RuntimeException(e);
				}
			}
			
			
			return Undefined.instance;
		}
		
		@Override
		public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
			throw new UnsupportedOperationException("cannot construct RestCalls");
		}
	}
	
	RestCallProvider(
		final AsyncHttpClient httpClient,
		final CurrentScriptContext context
	) {
		this.httpClient = httpClient;
		this.context = context;
	}
	
	Function createRestCall(final RestCallOptions options) {
		return new RestCall(options);
	}
}
