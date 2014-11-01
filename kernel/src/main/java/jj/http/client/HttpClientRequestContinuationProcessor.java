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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;

import javax.inject.Singleton;

import com.google.inject.Inject;

import jj.execution.TaskRunner;
import jj.script.ContinuationProcessor;
import jj.script.ContinuationState;

/**
 * @author jason
 *
 */
@Singleton
class HttpClientRequestContinuationProcessor implements ContinuationProcessor {
	
	private final HttpClient client;
	private final TaskRunner taskRunner;
	
	@Inject
	HttpClientRequestContinuationProcessor(
		final HttpClient client,
		final TaskRunner taskRunner
	) {
		this.client = client;
		this.taskRunner = taskRunner;
	}

	@Override
	public void process(ContinuationState continuationState) {
		final HttpClientRequest request = continuationState.continuationAs(HttpClientRequest.class);
		
		taskRunner.execute(new HttpClientTask("dispatching request " + request) {
			
			@Override
			protected void run() throws Exception {
				URI uri = request.uri();
				int port = uri.getPort() == -1 ? 80 : uri.getPort();
				client.connect(uri.getHost(), port).addListener(new ChannelFutureListener() {
					
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, request.method(), uri.getRawPath());
						future.channel().writeAndFlush(req);
					}
				});
			}
		});
	}

}
