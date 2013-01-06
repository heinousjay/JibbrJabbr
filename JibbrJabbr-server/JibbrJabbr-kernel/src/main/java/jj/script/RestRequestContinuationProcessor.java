package jj.script;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJExecutors;
import jj.JJRunnable;
import jj.continuation.RestRequest;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

public class RestRequestContinuationProcessor implements ContinuationProcessor {
	
	private final Logger log = LoggerFactory.getLogger(RestRequestContinuationProcessor.class);

	private final CurrentScriptContext context;
	
	private final AsyncHttpClient httpClient;
	
	private final JJExecutors executors;
	
	RestRequestContinuationProcessor(
		final CurrentScriptContext context,
		final AsyncHttpClient httpClient,
		final JJExecutors executors
	) {
		this.context = context;
		this.httpClient = httpClient;
		this.executors = executors;
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
				new JJRunnable(String.format("continuation listener for %s", restRequest.id())) {
					
					@Override
					protected void innerRun() throws Exception {
						context.restore(scriptContext);
						try {

							String body = response.get().getResponseBody();
							executors
								.scriptRunner()
								.restartAfterContinuation(
									restRequest.id(),
									continuationState.produceReturn(body)
								);
							
						} finally {
							context.end();
						}
					}
				}, executors.scriptExecutorFor(context.baseName()));
			
		} catch (IOException e) {
			log.error("trouble executing {}", restRequest);
			log.error("", e);
			executors
				.scriptRunner()
				.restartAfterContinuation(restRequest.id(), e);
		}
		
	}

}
