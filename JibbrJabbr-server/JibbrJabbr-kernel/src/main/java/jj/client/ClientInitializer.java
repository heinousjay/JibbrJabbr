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
package jj.client;

import org.picocontainer.MutablePicoContainer;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * @author jason
 *
 */
public class ClientInitializer {

public static MutablePicoContainer initialize(MutablePicoContainer container, boolean isTest) {
		
		ClientExecutor executor = new ClientExecutor();
	
		return container
			// needs to be smarter configuration? i at least should be
			// supplying the executor
			.addComponent(new AsyncHttpClientConfig.Builder()
				.setCompressionEnabled(true)
				.setUserAgent("JibbrJabbr RestCall subsystem/Netty 3.5.11Final")
				.setIOThreadMultiplier(1)
				.setFollowRedirects(true)
				.setScheduledExecutorService(executor)
				.setExecutorService(executor)
				.build())
			.addComponent(AsyncHttpClient.class);
		
	}
}
