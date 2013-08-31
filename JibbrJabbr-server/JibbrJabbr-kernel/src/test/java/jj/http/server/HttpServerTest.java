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

import static org.mockito.BDDMockito.given;
import jj.configuration.Configuration;
import jj.execution.MockJJNioEventLoopGroup;

import org.junit.Before;
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

	@Mock HttpServerChannelInitializer initializer;
	@Mock Configuration configuration;
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
			return new Binding[] {
				new Binding() {

					@Override
					public int port() {
						return 8080;
					}

					@Override
					public String host() {
						return null;
					}
				}
			};
		}
	};
	
	HttpServer httpServer;
	
	@Before
	public void before() {
		given(configuration.get(HttpServerSocketConfiguration.class)).willReturn(config);
		
		httpServer = new HttpServer(new MockJJNioEventLoopGroup(), initializer, configuration);
	}
	
	@Test
	public void test1() throws Exception {

		try {
			httpServer.start();
			
			// try to connect to 8080 and 8090 to prove it worked
			
		} finally {
			httpServer.stop();
		}
	}
}
