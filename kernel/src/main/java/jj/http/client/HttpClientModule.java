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

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.AsyncHttpProvider;
import org.asynchttpclient.providers.netty.NettyAsyncHttpProvider;

import jj.JJModule;

/**
 * @author jason
 *
 */
public class HttpClientModule extends JJModule {

	@Override
	protected void configure() {

		addAPIModulePath("/jj/http/client/api");
		
		dispatch().continuationOf(HttpClientRequest.class).to(HttpClientRequestContinuationProcessor.class);
		
		bindExecutor(HttpClientNioEventLoopGroup.class);
		
		// configuring the async-http-client to work from guice. not too hard, really,
		// and now the various levels of the API are available for injection.
		// the AsyncHttpClientConfigProvider handles bridging the configuration in
		// there are no singletons here so that new instances are configured on every
		// use, to ensure that configuration is live.  this means only Providers should
		// be injected
		bind(AsyncHttpClientConfig.class).toProvider(AsyncHttpClientConfigProvider.class);
		bind(AsyncHttpProvider.class).to(NettyAsyncHttpProvider.class);
		
		bindConfiguration().to(HttpClientConfiguration.class);
		
		try {
			
			bind(NettyAsyncHttpProvider.class)
				.toConstructor(NettyAsyncHttpProvider.class.getConstructor(AsyncHttpClientConfig.class));
			
			bind(AsyncHttpClient.class)
				.toConstructor(AsyncHttpClient.class.getConstructor(AsyncHttpProvider.class, AsyncHttpClientConfig.class));
			
		} catch (Exception e) {
			throw new AssertionError("broken build", e);
		}
	}

}
