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
package jj.http.client.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Map;

import javax.inject.Inject;

import jj.http.client.HttpClientRequest;
import jj.http.client.HttpRequester;
import jj.http.client.HttpRequester.Headers;
import jj.http.client.HttpResponseListener;
import jj.script.CurrentScriptEnvironment;

/**
 * @author jason
 *
 */
public class RestOperation extends HttpClientRequest {

	private final HttpRequester requester;
	private final CurrentScriptEnvironment env;
	private Headers headers;
	
	@Inject
	RestOperation(final HttpRequester requester, final CurrentScriptEnvironment env) {
		this.requester = requester;
		this.env = env;
	}
	
	@Override
	protected void begin() {
		try {
			headers.begin(new HttpResponseListener() {
				
				@Override
				protected void responseStart(HttpResponse response) {
					System.out.println(response);
				}
				
				@Override
				protected void bodyPart(ByteBuf bodyPart) {
					System.out.println(bodyPart.toString(UTF_8));
				}
				
				@Override
				protected void responseComplete(HttpHeaders trailingHeaders) {
					System.out.println("it completed");
				}
				
				@Override
				protected void requestErrored(Throwable cause) {
					
					cause.printStackTrace();
				}
			});
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	public void request(HttpMethod method, CharSequence uri, Map<CharSequence, CharSequence> params) {
		headers = requester.requestTo(uri.toString())
			.params(params)
			.method(method);
		
		env.preparedContinuation(this);
	}
}
