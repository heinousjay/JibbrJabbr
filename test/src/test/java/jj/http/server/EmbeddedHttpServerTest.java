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
package jj.http.server;

import static org.junit.Assert.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.Matchers.*;

import io.netty.handler.codec.http.HttpHeaderNames;

import javax.inject.Inject;

import jj.App;
import jj.ServerRoot;
import jj.http.server.EmbeddedHttpServer.ResponseReady;
import jj.testing.JibbrJabbrTestServer;

import jj.testing.Latch;
import org.junit.Rule;
import org.junit.Test;

/**
 * This is just a minor validation that the embedded server
 * works. the idea now is to test the http server using it
 * 
 * @author jason
 *
 */
public class EmbeddedHttpServerTest {
	
	@Inject
	EmbeddedHttpServer server;
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(ServerRoot.one, App.app1).injectInstance(this);

	@Test
	public void test() throws Throwable {
		EmbeddedHttpResponse response = server.request(new EmbeddedHttpRequest("/")).await(3, SECONDS);
		
		assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE), is("text/html; charset=UTF-8"));
		int contentLength = response.headers().getInt(HttpHeaderNames.CONTENT_LENGTH, -1);
		// this comparison works because it's actually ASCII.  if at some point characters outside
		// that range are returned then only the octets can be inspected
		assertThat(response.bodyContentAsString().length(), is(contentLength));
		assertThat(response.bodyContentAsBytes().length, is(contentLength));
	}
	
	@Test
	public void testCallback() throws Throwable {
		
		final Latch myLatch = new Latch(3);
		final AssertionError testFailures = new AssertionError("there were test failures");
		ResponseReady responseReady = response -> {
			try {
				String body = response.bodyContentAsString();
				int contentLength = response.headers().getInt(HttpHeaderNames.CONTENT_LENGTH, -1);
				assertThat(body.length(), is(contentLength)); // this only works because it's ASCII haha
			} catch (Throwable t) {
				testFailures.addSuppressed(t);
			} finally {
				myLatch.countDown();
			}
		};
		
		server.request(new EmbeddedHttpRequest("/"), responseReady);
		server.request(new EmbeddedHttpRequest("/0.txt"), responseReady);
		server.request(new EmbeddedHttpRequest("/jj.js"), responseReady);
		
		assertTrue(myLatch.await(3, SECONDS));
		
		if (testFailures.getSuppressed().length > 0) {
			throw testFailures;
		}
	}

}
