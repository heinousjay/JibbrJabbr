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
import static org.junit.Assert.*;

import java.net.ConnectException;

import javax.inject.Provider;
import javax.net.SocketFactory;

import jj.configuration.Configuration;
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

	@Mock Configuration configuration;
	
	@Mock HttpServerSwitch httpServerSwitch;
	
	@Mock Publisher publisher;
	
	HttpServerSocketConfiguration config = new HttpServerSocketConfiguration() {
		
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
		public Binding[] bindings() {
			return new Binding[] { new Binding(8080), new Binding("localhost", 8090) };
		}
	};
	
	// TODO validate the configuration is used correctly.  but how? spy via factory for the ServerBootstrap?
	
	@Test
	public void testServerStart() throws Exception {
		
		// this is all in one test for order control.  we need to be assured that overriding the port
		// through an argument prevents configured bindings from being used, but the http server
		// startup/shutdown is asynchronous, so we need to order these tests or they execute too quickly
		// to prove correctness - if the configured bindings are made first, then the override binding
		// is made, the configured bindings won't yet be shut down by the test time, and we won't be
		// able to prove that the configured bindings weren't made
		
		// TODO - may need to recast this test in any case, since it's not guaranteed that 8080/8090/5678 are available
		
		given(configuration.get(HttpServerSocketConfiguration.class)).willReturn(config);
		given(httpServerSwitch.on()).willReturn(true);
		given(httpServerSwitch.port()).willReturn(5678);
		HttpServer httpServer = new HttpServer(
			new MockJJNioEventLoopGroup(),
			new HttpServerChannelInitializer(engineProvider),
			configuration,
			httpServerSwitch,
			publisher
		);
		
		try {
			// when
			httpServer.start();
			
			
			// then
			SocketFactory.getDefault().createSocket("localhost", 5678).close();
			
			try {
				SocketFactory.getDefault().createSocket("localhost", 8080);
				fail("should not have a binding to 8080!");
			} catch (ConnectException e) {
				// yay!
			}
			
		} finally {
			httpServer.stop(null);
		}
		
		// given
		given(httpServerSwitch.port()).willReturn(-1);
		httpServer = new HttpServer(
			new MockJJNioEventLoopGroup(),
			new HttpServerChannelInitializer(engineProvider),
			configuration,
			httpServerSwitch,
			publisher
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
