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
	
	HttpServer httpServer;
	
	@Before
	public void before() {
		httpServer = new HttpServer(new MockJJNioEventLoopGroup(), initializer, configuration);
	}
	
	@Test
	public void test1() throws Exception {

		try {
			httpServer.start();
		} finally {
			httpServer.stop();
		}
	}
	
	@Test
	public void test2() throws Exception {

		try {
			httpServer.start();
		} finally {
			httpServer.stop();
		}
	}
	
	@Test
	public void test3() throws Exception {

		try {
			httpServer.start();
		} finally {
			httpServer.stop();
		}
	}

}
