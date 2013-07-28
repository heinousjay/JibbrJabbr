package jj.script;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.execution.JJExecutors;
import jj.execution.JJRunnable;
import jj.http.client.HttpClient;
import jj.http.client.JJHttpClientResponse;

@Singleton
class RestRequestContinuationProcessor implements ContinuationProcessor {
	
	private final Logger log = LoggerFactory.getLogger(RestRequestContinuationProcessor.class);

	private final CurrentScriptContext context;
	
	private final HttpClient httpClient;
	
	private final JJExecutors executors;
	
	@Inject
	RestRequestContinuationProcessor(
		final CurrentScriptContext context,
		final HttpClient httpClient,
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
		httpClient.execute(restRequest.request()).addListener(
			new GenericFutureListener<Future<JJHttpClientResponse>>() {

				@Override
				public void operationComplete(final Future<JJHttpClientResponse> future) throws Exception {
					
					executors.scriptExecutorFor(context.baseName()).submit(
						new JJRunnable("REST response with id [" + restRequest.id() + "]") {
							
							@Override
							public void run() {
								context.restore(scriptContext);
								try {
									executors
										.scriptRunner()
										.restartAfterContinuation(
											restRequest.id(),
											future.get()
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
						}
					);
				}
				
				
			}
		);
	}

}
