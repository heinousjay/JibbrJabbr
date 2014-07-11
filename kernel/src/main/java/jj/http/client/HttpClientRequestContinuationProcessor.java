/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.http.client;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHandler.STATE;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;

import jj.script.ContinuationProcessor;
import jj.script.ContinuationState;

/**
 * @author jason
 *
 */
@Singleton
class HttpClientRequestContinuationProcessor implements ContinuationProcessor {
	
	private final Provider<AsyncHttpClient> httpClient;
	
	@Inject
	HttpClientRequestContinuationProcessor(final Provider<AsyncHttpClient> httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public void process(ContinuationState continuationState) {
		final HttpClientRequest c = continuationState.continuationAs(HttpClientRequest.class);
		
		// dispatch the request
		try {
			httpClient.get().executeRequest(c.request(), new AsyncHandler<Void>() {

				@Override
				public void onThrowable(Throwable t) {
					c.pendingKey().resume(t);
				}

				@Override
				public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
					// accumulate the body part into the response
					return STATE.CONTINUE;
				}

				@Override
				public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
					
					return STATE.CONTINUE;
				}

				@Override
				public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
					
					return STATE.CONTINUE;
				}

				@Override
				public Void onCompleted() throws Exception {
					c.pendingKey().resume("well, we tried something");
					return null;
				}
				
			});
		} catch (IOException e) {
			c.pendingKey().resume(e);
		}
		
		
	}

}
