package jj.script;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.client.HttpClient;
import jj.http.client.JJHttpClientResponse;

@Singleton
class RestRequestContinuationProcessor implements ContinuationProcessor {

	private final CurrentScriptContext context;
	
	private final HttpClient httpClient;
	
	private final ScriptRunnerInternal scriptRunner;
	
	@Inject
	RestRequestContinuationProcessor(
		final CurrentScriptContext context,
		final HttpClient httpClient,
		final ScriptRunnerInternal scriptRunner
	) {
		this.context = context;
		this.httpClient = httpClient;
		this.scriptRunner = scriptRunner;
	}

	@Override
	public void process(final ContinuationState continuationState) {
		final ScriptContext scriptContext = context.save();
		final RestRequest restRequest = continuationState.continuableAs(RestRequest.class);
		httpClient.execute(restRequest.request()).addListener(
			new GenericFutureListener<Future<JJHttpClientResponse>>() {

				@Override
				public void operationComplete(final Future<JJHttpClientResponse> future) throws Exception {
					
					try {
						scriptRunner.submit("REST response with id [" + restRequest.pendingKey() + "]", scriptContext, restRequest.pendingKey(), future.get());
					} catch (InterruptedException | CancellationException e) {
						// ignore this, we're shutting down
					} catch (ExecutionException e) {
						scriptRunner.submit("REST response with id [" + restRequest.pendingKey() + "]", scriptContext, restRequest.pendingKey(), e.getCause());
					}
				}
			}
		);
	}

}
