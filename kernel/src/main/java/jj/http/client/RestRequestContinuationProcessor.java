package jj.http.client;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.script.ContinuationProcessor;
import jj.script.ContinuationState;

@Singleton
class RestRequestContinuationProcessor implements ContinuationProcessor {
	
	private final HttpClient httpClient;
	
	@Inject
	RestRequestContinuationProcessor(
		final HttpClient httpClient
	) {
		this.httpClient = httpClient;
	}

	@Override
	public void process(final ContinuationState continuationState) {
		final RestRequest restRequest = continuationState.continuationAs(RestRequest.class);
		httpClient.execute(restRequest.request()).addListener(
			new GenericFutureListener<Future<JJHttpClientResponse>>() {

				@Override
				public void operationComplete(final Future<JJHttpClientResponse> future) throws Exception {
					
					try {
						restRequest.pendingKey().resume(future.get());
					} catch (InterruptedException | CancellationException e) {
						// ignore this, we're shutting down
					} catch (ExecutionException e) {
						restRequest.pendingKey().resume(e.getCause());
					}
				}
			}
		);
	}

}