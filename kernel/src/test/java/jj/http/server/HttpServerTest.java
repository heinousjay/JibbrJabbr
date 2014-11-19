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

import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;
import javax.net.SocketFactory;

import jj.event.Publisher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpServerTest {
	
	Provider<EngineHttpHandler> engineProvider = new Provider<EngineHttpHandler>() {
		
		@Override
		public EngineHttpHandler get() {
			return mock(EngineHttpHandler.class);
		}
	};
	
	@Mock HttpServerSwitch httpServerSwitch;
	
	@Mock Publisher publisher;
	
	@Mock UncaughtExceptionHandler uncaughtExceptionHandler;
	
	HttpServerSocketConfiguration configuration = new HttpServerSocketConfiguration() {
		
		@Override
		public int timeout() {
			return 10000;
		}
		
		@Override
		public boolean tcpNoDelay() {
			return true;
		}
		
		@Override
		public int sendBufferSize() {
			return 65536;
		}
		
		@Override
		public boolean reuseAddress() {
			return true;
		}
		
		@Override
		public int receiveBufferSize() {
			return 65536;
		}
		
		@Override
		public boolean keepAlive() {
			return true;
		}
		
		@Override
		public int backlog() {
			return 12;
		}
		
		@Override
		public List<Binding> bindings() {
			return Arrays.asList(new Binding(8080), new Binding("localhost", 8090));
		}
	};
	
	HttpServer httpServer;
	
	// TODO validate the configuration is used correctly.  but how? spy via factory for the ServerBootstrap?
	
	@Test
	public void testServer() throws Exception {
		
		// in the same test to ensure that the 'off' test runs first
		
		// given
		httpServer = new HttpServer(
			new MockHttpServerNioEventLoopGroup(),
			new HttpServerChannelInitializer(engineProvider),
			configuration,
			httpServerSwitch,
			publisher,
			uncaughtExceptionHandler
		);
		
		// when
		httpServer.start();
		
		// then
		try {
			SocketFactory.getDefault().createSocket("localhost", 8080).close();
			fail("NO NO NO");
		} catch (ConnectException e) {
			assertThat(e.getMessage(), is("Connection refused"));
		}
		
		
		// given
		given(httpServerSwitch.on()).willReturn(true);
		httpServer = new HttpServer(
			new MockHttpServerNioEventLoopGroup(),
			new HttpServerChannelInitializer(engineProvider),
			configuration,
			httpServerSwitch,
			publisher,
			uncaughtExceptionHandler
		);

		try {
			// when
			httpServer.start();
			
			
			// then
			SocketFactory.getDefault().createSocket("localhost", 8080).close();
			SocketFactory.getDefault().createSocket("localhost", 8090).close();
			
		} finally {
			httpServer.stop(null);
		}
	}
}
