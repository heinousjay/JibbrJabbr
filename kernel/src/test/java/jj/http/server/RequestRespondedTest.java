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

import static java.nio.charset.StandardCharsets.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.net.SocketAddress;

import jj.Version;
import jj.event.MockPublisher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestRespondedTest {
	
	@Mock Logger logger;
	@Mock Version version;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) ChannelHandlerContext ctx;
	@Mock SocketAddress socketAddress;
	MockPublisher publisher;

	@Test
	public void testAccessLog() throws IOException {
		given(ctx.channel().remoteAddress()).willReturn(socketAddress);
		given(socketAddress.toString()).willReturn("1.1.1.1");
		publisher = new MockPublisher();
		HttpServerRequestImpl request =
			new HttpServerRequestImpl(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"), ctx);
		
		HttpServerResponseImpl response = new HttpServerResponseImpl(version, request, ctx, publisher);
		
		// given
		given(logger.isInfoEnabled()).willReturn(true);
		given(logger.isTraceEnabled()).willReturn(true);
		String location = "home";
		byte[] bytes = "this is the contents".getBytes(UTF_8);
		long length = 100L;
		
		
		response.status(HttpResponseStatus.FOUND)
			.header(HttpHeaderNames.ACCEPT_RANGES, HttpHeaderValues.BYTES)
			.header(HttpHeaderNames.LOCATION, location)
			.header(HttpHeaderNames.CONTENT_LENGTH, length)
			.content(bytes)
			.end();
		
		assertThat(publisher.events.size(), is(1));
		assertThat(publisher.events.get(0), is(instanceOf(RequestResponded.class)));
		
		((RequestResponded)publisher.events.get(0)).describeTo(logger);
		
		// have to do this outside the verification or mockito gets all jacked up inside
		String remoteAddress = ctx.channel().remoteAddress().toString();
		
		verify(logger).info(
			eq("{} - - {} \"{} {} {}\" {} {} {} {}"),
			eq(remoteAddress),
			anyString(), // the date, not going to try to make this work
			eq(request.method()),
			eq(request.request().uri()),
			eq(request.request().protocolVersion()),
			eq(response.status().code()),
			eq(String.valueOf(length)),
			eq("-"),
			eq("-")
		);
	}

}
