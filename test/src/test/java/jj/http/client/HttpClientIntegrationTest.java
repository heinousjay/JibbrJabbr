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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicReference;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;

import javax.inject.Inject;

import jj.App;
import jj.JJModule;
import jj.ServerRoot;
import jj.http.client.api.RestOperation;
import jj.server.BindsServerPath;
import jj.testing.JibbrJabbrTestServer;

import jj.testing.Latch;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore // this test is currently pointless
public class HttpClientIntegrationTest {

	@Rule
	public JibbrJabbrTestServer server =
		new JibbrJabbrTestServer(ServerRoot.one, App.httpClient)
			.withHttp()
			.withModule(new TestHelperModule())
			.injectInstance(this);
	
	@Inject HttpRequester requester;
	
	@Inject RestOperation restOperation;
	
	@Test
	public void testHttpRequester() throws Throwable {
		
		final AtomicReference<String> response = new AtomicReference<>();
		final AtomicReference<Throwable> cause = new AtomicReference<>();
		final Latch latch = new Latch(1);
		
		requester.requestTo(server.baseUrl() + "/test2.txt")
			.get()
			.begin(new HttpResponseListener() {
				
				@Override
				protected void responseStart(HttpResponse r) {
					//System.out.println(response);
				}

				@Override
				protected void requestErrored(Throwable c) {
					cause.set(c);
					latch.countDown();
				}
				
				@Override
				protected void bodyPart(ByteBuf bodyPart) {
					response.set(bodyPart.toString(UTF_8));
				}
				
				@Override
				protected void responseComplete(HttpHeaders trailingHeaders) {
					latch.countDown();
				}
				
			});
		
		
		latch.await(500, MILLISECONDS);
		if (cause.get() != null) {
			throw cause.get();
		}
		assertThat(response.get(), is("I am the text"));
	}

	private static class TestHelperModule extends JJModule implements BindsServerPath {

		@Override
		protected void configure() {
			bindAPIModulePath("/http/client/test");
		}
	}
}
