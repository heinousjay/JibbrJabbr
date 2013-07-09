package jj.script;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.execution.JJExecutors;
import jj.execution.JJRunnable;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

@Singleton
class RestRequestContinuationProcessor implements ContinuationProcessor {
	
	private final Logger log = LoggerFactory.getLogger(RestRequestContinuationProcessor.class);

	private final CurrentScriptContext context;
	
	private final AsyncHttpClient httpClient;
	
	private final JJExecutors executors;
	
	@Inject
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
				new JJRunnable("REST response with id [" + restRequest.id() + "]") {
							
					@Override
					public void run() {
						context.restore(scriptContext);
						try {
							executors
								.scriptRunner()
								.restartAfterContinuation(
									restRequest.id(),
									response.get()
								);
						} catch (InterruptedException | CancellationException e) {
							// ignore this, we're shutting down
						} catch (ExecutionException e) {
							executors
							.scriptRunner()
							.restartAfterContinuation(
								restRequest.id(),
								e.getCause()
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
