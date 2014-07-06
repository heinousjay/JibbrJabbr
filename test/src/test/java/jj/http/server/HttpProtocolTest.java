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

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;

import javax.inject.Inject;

import com.google.inject.Provider;

import javax.inject.Singleton;

import jj.App;
import jj.event.Publisher;
import jj.execution.TaskRunner;
import jj.http.server.methods.HttpMethodHandler;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class HttpProtocolTest {
	
	public static class ProtocolTestResponseAdapter extends ChannelOutboundHandlerAdapter {
		
		private final EmbeddedHttpResponse response;
		
		ProtocolTestResponseAdapter(final EmbeddedHttpResponse response) {
			this.response = response;
		}
		
		@Override
		public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			response.responseReady();
			ctx.close(promise);
		}
		
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			if (msg instanceof HttpResponse) {
				response.response = (HttpResponse)msg;
			}
			ctx.write(msg, promise);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			response.error = cause;
		}
	}
	
	@Singleton
	public static class ProtocolTestHttpServer {
		
		private final TaskRunner taskRunner;
		private final Publisher publisher;
		private final Map<HttpMethod, Provider<HttpMethodHandler>> methodHandlers;
		
		@Inject
		ProtocolTestHttpServer(
			final TaskRunner taskRunner,
			final Publisher publisher,
			final Map<HttpMethod, Provider<HttpMethodHandler>> methodHandlers
		) {
			this.taskRunner = taskRunner;
			this.publisher = publisher;
			this.methodHandlers = methodHandlers;
		}
		
		public EmbeddedHttpResponse request(final String request) {

			final EmbeddedHttpResponse response = new EmbeddedHttpResponse();

			taskRunner.execute(new HttpServerTask("ProtocolTestHttpServer request") {

				@Override
				protected void run() throws Exception {
					EmbeddedChannel channel = new EmbeddedChannel(
						new ProtocolTestResponseAdapter(response),
						// timeout handler
						new HttpRequestDecoder(),
						new HttpRequestListeningHandler(publisher, methodHandlers)
					);
					channel.writeInbound(Unpooled.copiedBuffer(request, US_ASCII));
				}
			});

			return response;
		}
	}
	
	@Inject
	ProtocolTestHttpServer testServer;
	
	@Rule
	public final JibbrJabbrTestServer jj = new JibbrJabbrTestServer(App.two).injectInstance(this);
	
	@Test
	public void test() throws Throwable {
		
		testFirstLineErrors();
		testGet();
		testOptions();
	}
	
	private void testGet() throws Throwable {
		EmbeddedHttpResponse response = testServer.request("GET / HTTP/1.1\r\n\r\n");
		
		response.await(2, SECONDS);
		
		assertThat(response.status(), is(OK));
	}

	private void testFirstLineErrors() throws Throwable {
		EmbeddedHttpResponse response = testServer.request("this will fail\r\n\r\n");
		
		response.await(2, SECONDS);
		
		assertThat(response.status(), is(BAD_REQUEST));
		
		response = testServer.request("this too\r\n\r\n");
		
		response.await(2, SECONDS);
		
		assertThat(response.status(), is(BAD_REQUEST));
		
		response = testServer.request("not implemented HTTP/1.1\r\n\r\n");
		
		response.await(2, SECONDS);
		
		assertThat(response.status(), is(NOT_IMPLEMENTED));
	}
	
	private void testOptions() throws Throwable {
		EmbeddedHttpResponse response = testServer.request("OPTIONS /something HTTP/1.1\r\n\r\n");
		
		response.await(2, SECONDS);
		
		assertThat(response.status(), is(OK));
		assertThat(response.headers().get(HttpHeaders.Names.ALLOW), is("GET,HEAD,TRACE,OPTIONS"));
	}
	
}
