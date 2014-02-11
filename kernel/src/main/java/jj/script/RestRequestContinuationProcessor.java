package jj.script;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.JJExecutor;
import jj.http.client.HttpClient;
import jj.http.client.JJHttpClientResponse;

@Singleton
class RestRequestContinuationProcessor implements ContinuationProcessor {

	private final JJExecutor executor;
	
	private final HttpClient httpClient;
	
	@Inject
	RestRequestContinuationProcessor(
		final HttpClient httpClient,
		final JJExecutor executor
	) {
		this.executor = executor;
		this.httpClient = httpClient;
	}

	@Override
	public void process(final ContinuationState continuationState) {
		final RestRequest restRequest = continuationState.continuableAs(RestRequest.class);
		httpClient.execute(restRequest.request()).addListener(
			new GenericFutureListener<Future<JJHttpClientResponse>>() {

				@Override
				public void operationComplete(final Future<JJHttpClientResponse> future) throws Exception {
					
					try {
						executor.resume(restRequest.pendingKey(), future.get());
					} catch (InterruptedException | CancellationException e) {
						// ignore this, we're shutting down
					} catch (ExecutionException e) {
						executor.resume(restRequest.pendingKey(), e.getCause());
					}
				}
			}
		);
	}

}
