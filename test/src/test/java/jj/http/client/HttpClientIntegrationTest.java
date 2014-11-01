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
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import javax.inject.Inject;

import jj.App;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class HttpClientIntegrationTest {

	@Rule
	public JibbrJabbrTestServer server =
		new JibbrJabbrTestServer(App.one)
			.injectInstance(this);
	
	@Inject HttpClient client;
	
	@Ignore
	@Test
	public void test() throws Exception {
		client.connect("jibbrjabbr.com", 80).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
				request.headers()
					.add(HttpHeaders.Names.HOST, "jibbrjabbr.com")
					.add(HttpHeaders.Names.ACCEPT, "text/html");
				future.channel().writeAndFlush(request);
			}
		});
		
		Thread.sleep(1000);
	}
}
