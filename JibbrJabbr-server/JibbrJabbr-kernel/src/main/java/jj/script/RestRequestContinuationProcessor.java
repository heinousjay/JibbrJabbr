package jj.script;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.JJExecutors;
import jj.execution.ScriptTask;
import jj.http.client.HttpClient;
import jj.http.client.JJHttpClientResponse;

@Singleton
class RestRequestContinuationProcessor implements ContinuationProcessor {

	private final CurrentScriptContext context;
	
	private final HttpClient httpClient;
	
	private final JJExecutors executors;
	
	private final ScriptRunner scriptRunner;
	
	@Inject
	RestRequestContinuationProcessor(
		final CurrentScriptContext context,
		final HttpClient httpClient,
		final JJExecutors executors,
		final ScriptRunner scriptRunner
	) {
		this.context = context;
		this.httpClient = httpClient;
		this.executors = executors;
		this.scriptRunner = scriptRunner;
	}

	@Override
	public void process(final ContinuationState continuationState) {
		final ScriptContext scriptContext = context.save();
		final RestRequest restRequest = continuationState.restRequest();
		httpClient.execute(restRequest.request()).addListener(
			new GenericFutureListener<Future<JJHttpClientResponse>>() {

				@Override
				public void operationComplete(final Future<JJHttpClientResponse> future) throws Exception {
					
					executors.execute(
						new ScriptTask("REST response with id [" + restRequest.id() + "]", context.baseName()) {
							
							@Override
							protected void run() throws Exception {
								context.restore(scriptContext);
								try {
									scriptRunner.restartAfterContinuation(restRequest.id(), future.get());
								} catch (InterruptedException | CancellationException e) {
									// ignore this, we're shutting down
								} catch (ExecutionException e) {
									scriptRunner.restartAfterContinuation(restRequest.id(), e.getCause());
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
