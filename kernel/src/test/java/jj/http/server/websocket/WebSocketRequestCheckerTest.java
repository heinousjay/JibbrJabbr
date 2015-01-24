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
package jj.http.server.websocket;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import jj.http.server.websocket.WebSocketRequestChecker;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketRequestCheckerTest {
	
	final String socketUri = "/1234567890123456789012345678901234567890/something.socket";
	
	WebSocketRequestChecker wsrc;
	FullHttpRequest request;
	
	@Before
	public void before() {
		wsrc = new WebSocketRequestChecker();
	}

	@Test
	public void testMatchesWebSocketRequest() {

		request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, socketUri);
		request.headers()
			.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE)
			.add(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET);
		assertThat(wsrc.isWebSocketRequest(request), is(true));

		request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/some/uri");
		request.headers()
			.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE)
			.add(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET);
		assertThat(wsrc.isWebSocketRequest(request), is(false));
		
		request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, socketUri);
		assertThat(wsrc.isWebSocketRequest(request), is(false));
	}
}
