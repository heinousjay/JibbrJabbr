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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import jj.script.ScriptBundleFinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionMakerTest {
	
	final String socketUri = "/1234567890123456789012345678901234567890/something.socket";
	
	WebSocketConnectionMaker wscm;
	@Mock Injector parentInjector;
	@Captor ArgumentCaptor<Module> moduleCaptor;
	@Mock ScriptBundleFinder scriptBundleFinder;
	FullHttpRequest request;
	
	@Before
	public void before() {
		wscm = new WebSocketConnectionMaker(parentInjector, scriptBundleFinder);
	}

	@Test
	public void testMatchesWebSocketRequest() {

		request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, socketUri);
		request.headers()
			.add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE)
			.add(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
		assertThat(wscm.isWebSocketRequest(request), is(true));

		request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/some/uri");
		request.headers()
			.add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE)
			.add(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
		assertThat(wscm.isWebSocketRequest(request), is(false));
		
		request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, socketUri);
		assertThat(wscm.isWebSocketRequest(request), is(false));
	}

}
