package jj.script;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJExecutors;
import jj.JJRunnable;
import jj.hostapi.ScriptJSON;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

@Singleton
class RestRequestContinuationProcessor implements ContinuationProcessor {
	
	private final Logger log = LoggerFactory.getLogger(RestRequestContinuationProcessor.class);

	private final CurrentScriptContext context;
	
	private final AsyncHttpClient httpClient;
	
	private final JJExecutors executors;
	
	private final ScriptJSON json;
	
	@Inject
	RestRequestContinuationProcessor(
		final CurrentScriptContext context,
		final AsyncHttpClient httpClient,
		final JJExecutors executors,
		final ScriptJSON json
	) {
		this.context = context;
		this.httpClient = httpClient;
		this.executors = executors;
		this.json = json;
	}
	
	@Override
	public ContinuationType type() {
		return ContinuationType.AsyncHttpRequest;
	}

	@Override
	public void process(final ContinuationState continuationState) {
		final ScriptContext scriptContext = context.save();
		final RestRequest restRequest = continuationState.restRequest();
		try {
			final ListenableFuture<Response> response = httpClient.executeRequest(restRequest.request());
			response.addListener(
				executors.prepareTask(new JJRunnable("REST response with id [" + restRequest.id() + "]") {
					
					@Override
					public void run() throws Exception {
						context.restore(scriptContext);
						try {
							// TODO obviously this isn't right yet
							String body = response.get().getResponseBody();
							executors
								.scriptRunner()
								.restartAfterContinuation(
									restRequest.id(),
									json.parse(body)
								);
							
						} catch (Exception e) {
							log.error("trouble executing {}", restRequest);
							throw e;
						} finally {
							context.end();
						}
					}
				}), executors.scriptExecutorFor(context.baseName()));
			
		} catch (IOException e) {
			log.error("trouble executing {}", restRequest);
			log.error("", e);
			executors
				.scriptRunner()
				.restartAfterContinuation(restRequest.id(), e);
		}
		
	}

}
