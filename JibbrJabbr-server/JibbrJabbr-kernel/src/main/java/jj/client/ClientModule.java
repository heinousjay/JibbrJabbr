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

import javax.inject.Singleton;

import jj.JJModule;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * @author jason
 *
 */
public class ClientModule extends JJModule {
	
	@Override
	protected void configure() {
	
		bind(AsyncHttpClientConfig.class).toProvider(AsyncHttpClientConfigProvider.class);
		
		try {
			bind(AsyncHttpClient.class)
				.toConstructor(
					AsyncHttpClient.class.getConstructor(AsyncHttpClientConfig.class)
				)
				.in(Singleton.class);
		} catch (Exception e) {
			throw new AssertionError("couldn't get the right constructor", e);
		}
	}
}
